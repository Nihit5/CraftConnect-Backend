package com.nihit.craft_connect.service.order.impl;

import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.dto.order.VendorOrderItemPojo;
import com.nihit.craft_connect.dto.order.VendorPaymentDetailPojo;
import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.OrderProduct;
import com.nihit.craft_connect.entity.Payment;
import com.nihit.craft_connect.entity.Product;
import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.OrderProductRepository;
import com.nihit.craft_connect.repository.OrderRepository;
import com.nihit.craft_connect.repository.PaymentRepository;
import com.nihit.craft_connect.repository.ProductRepository;
import com.nihit.craft_connect.service.file.FileService;
import com.nihit.craft_connect.service.order.VendorOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorOrderServiceImpl implements VendorOrderService {

    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserDetailConfig userDetailConfig;
    private final FileService fileService;
    private final OrderRepository orderRepository;

    private static final Set<OrderStatus> VENDOR_SETTABLE_STATUSES =
            Set.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private static final List<OrderStatus> FORWARD_SEQUENCE = List.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderItemPojo> getMyOrderItems() {
        Long vendorId = userDetailConfig.getLoggedInUserId();
        return orderProductRepository.findByProduct_User_IdOrderByOrder_CreatedDateDesc(vendorId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderItemPojo> getMyOrderItemsByProduct(Long productId) {
        Long vendorId = userDetailConfig.getLoggedInUserId();
        return orderProductRepository
                .findByProduct_IdAndProduct_User_IdOrderByOrder_CreatedDateDesc(productId, vendorId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void updateItemStatus(Long orderProductId, OrderStatus newStatus, String cancellationReason) {
        Long vendorId = userDetailConfig.getLoggedInUserId();

        if (!VENDOR_SETTABLE_STATUSES.contains(newStatus)) {
            throw new AppException("Invalid status. Allowed: CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED.");
        }

        OrderProduct orderProduct = orderProductRepository
                .findByIdAndProduct_User_Id(orderProductId, vendorId)
                .orElseThrow(() -> new AppException("Order item not found or does not belong to you."));

        OrderStatus current = orderProduct.getItemStatus();

        if (current == null || current == OrderStatus.PAYMENT_FAILED) {
            throw new AppException("Cannot update delivery status until payment is confirmed.");
        }
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new AppException("This item's status is already final and cannot be changed.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            if (current == OrderStatus.SHIPPED) {
                throw new AppException("Cannot cancel an item that has already been shipped.");
            }
            cancelItem(orderProduct, cancellationReason);
        } else if (!isForwardMove(current, newStatus)) {
            throw new AppException("Cannot move status from " + current + " to " + newStatus + ".");
        } else {
            orderProduct.setItemStatus(newStatus);
            orderProductRepository.save(orderProduct);
            if (newStatus == OrderStatus.DELIVERED) {
                markPaymentSuccessOnDelivery(orderProduct.getOrder());
            }
        }

        recalculateOrderStatus(orderProduct.getOrder());
    }

    private void markPaymentSuccessOnDelivery(Order order) {
        Payment payment = order.getPayment();
        if (payment == null) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setModifiedDate(new Timestamp(System.currentTimeMillis()));
            paymentRepository.save(payment);
        }
    }
    private void cancelItem(OrderProduct orderProduct, String cancellationReason) {
        // 1. restock — this part is always safe to automate regardless of payment method
        Product product = orderProduct.getProduct();
        product.setQuantity(product.getQuantity() + orderProduct.getQuantity());
        productRepository.save(product);

        orderProduct.setItemStatus(OrderStatus.CANCELLED);
        orderProductRepository.save(orderProduct);

        Order order = orderProduct.getOrder();
        Payment payment = order.getPayment();

        // 2. money handling — depends on how the item was paid for
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            log.info("Item {} cancelled (COD, no refund needed)", orderProduct.getId());
        } else if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            double owed = orderProduct.getSubTotal();
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            payment.setRefundAmount(
                    (payment.getRefundAmount() != null ? payment.getRefundAmount() : 0.0) + owed
            );
            String note = "Item #" + orderProduct.getId() + " cancelled by vendor. Refund owed: Rs. " + owed
                    + (cancellationReason != null ? ". Reason: " + cancellationReason : "");
            payment.setRefundNotes(
                    (payment.getRefundNotes() != null ? payment.getRefundNotes() + " | " : "") + note
            );
            payment.setModifiedDate(new Timestamp(System.currentTimeMillis()));
            paymentRepository.save(payment);

            log.warn("Refund owed for order {}: Rs. {} — process manually via {} merchant panel",
                    order.getId(), owed, order.getPaymentMethod());
        }
        // order-level recalculation now happens once, centrally, in recalculateOrderStatus —
        // removed the duplicate "allCancelled" check that used to live here
    }

    // NEW — single source of truth for deriving Order.status from all its OrderProduct.itemStatus values
    private void recalculateOrderStatus(Order order) {
        List<OrderStatus> itemStatuses = order.getOrderProducts().stream()
                .map(OrderProduct::getItemStatus)
                .toList();

        // don't touch orders still awaiting payment or that failed payment —
        // those are controlled by the payment flow, not by vendor actions
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }

        boolean allDelivered = itemStatuses.stream().allMatch(s -> s == OrderStatus.DELIVERED);
        boolean allCancelled = itemStatuses.stream().allMatch(s -> s == OrderStatus.CANCELLED);
        boolean anyShipped = itemStatuses.stream().anyMatch(s -> s == OrderStatus.SHIPPED);
        boolean anyProcessing = itemStatuses.stream().anyMatch(s -> s == OrderStatus.PROCESSING);

        OrderStatus newOrderStatus;
        if (allDelivered) {
            newOrderStatus = OrderStatus.DELIVERED;
        } else if (allCancelled) {
            newOrderStatus = OrderStatus.CANCELLED;
        } else if (anyShipped) {
            newOrderStatus = OrderStatus.SHIPPED;
        } else if (anyProcessing) {
            newOrderStatus = OrderStatus.PROCESSING;
        } else {
            newOrderStatus = OrderStatus.CONFIRMED; // mixed CONFIRMED/CANCELLED items, none shipped/processing yet
        }

        if (order.getStatus() != newOrderStatus) {
            order.setStatus(newOrderStatus);
            order.setModifiedDate(new Timestamp(System.currentTimeMillis()));
            orderRepository.save(order);
        }
    }

    private boolean isForwardMove(OrderStatus current, OrderStatus next) {
        if (current == null || next == null) {
            return false;
        }
        int currentIdx = FORWARD_SEQUENCE.indexOf(current);
        int nextIdx = FORWARD_SEQUENCE.indexOf(next);
        return currentIdx != -1 && nextIdx != -1 && nextIdx > currentIdx;
    }

    private VendorOrderItemPojo mapToResponse(OrderProduct op) {
        Order order = op.getOrder();
        VendorOrderItemPojo pojo = new VendorOrderItemPojo();
        pojo.setOrderProductId(op.getId());
        pojo.setOrderId(order.getId());
        pojo.setOrderedDate(order.getCreatedDate());
        pojo.setProductId(op.getProduct().getId());
        pojo.setProductName(op.getProduct().getName());
        pojo.setProductImage(fileService.extractFileName(op.getProduct().getImagePath()));
        pojo.setQuantity(op.getQuantity());
        pojo.setPriceAtPurchase(op.getPriceAtPurchase());
        pojo.setSubTotal(op.getSubTotal());
        pojo.setItemStatus(op.getItemStatus());
        pojo.setPaymentMethod(order.getPaymentMethod());
        pojo.setPaymentStatus(order.getPayment() != null ? order.getPayment().getStatus() : null);
        pojo.setRecipientName(order.getRecipientName());
        pojo.setMobileNumber(order.getMobileNumber());
        pojo.setProvince(order.getProvince());
        pojo.setDistrict(order.getDistrict());
        pojo.setShippingAddressLine(order.getShippingAddressLine());
        pojo.setLandmark(order.getLandmark());
        return pojo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorPaymentDetailPojo> getMyPaymentDetails() {
        Long vendorId = userDetailConfig.getLoggedInUserId();
        return orderProductRepository.findByProduct_User_IdOrderByOrder_CreatedDateDesc(vendorId)
                .stream()
                .map(this::mapToPaymentDetail)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorOrderItemPojo getMyOrderItemDetail(Long orderProductId) {
        Long vendorId = userDetailConfig.getLoggedInUserId();

        OrderProduct orderProduct = orderProductRepository
                .findByIdAndProduct_User_Id(orderProductId, vendorId)
                .orElseThrow(() -> new AppException("Order item not found or does not belong to you."));

        return mapToResponse(orderProduct);
    }

    private VendorPaymentDetailPojo mapToPaymentDetail(OrderProduct op) {
        Order order = op.getOrder();
        Payment payment = order.getPayment();

        VendorPaymentDetailPojo pojo = new VendorPaymentDetailPojo();
        pojo.setOrderId(order.getId());
        pojo.setOrderProductId(op.getId());

        pojo.setProductId(op.getProduct().getId());
        pojo.setProductName(op.getProduct().getName());
        pojo.setQuantity(op.getQuantity());
        pojo.setItemSubTotal(op.getSubTotal());
        pojo.setProductPrice(op.getProduct().getPrice());
        pojo.setCustomerName(order.getShippingAddress().getRecipientName());
        pojo.setMobileNumber(order.getShippingAddress().getMobileNumber());

        pojo.setOrderTotalAmount(order.getTotalAmount());

        if (payment != null) {
            pojo.setPaymentMethod(payment.getMethod());
            pojo.setPaymentStatus(payment.getStatus());
            pojo.setPaymentAmount(payment.getAmount());
            pojo.setPaymentDate(payment.getModifiedDate());
            pojo.setRefundAmount(payment.getRefundAmount());
            pojo.setRefundNotes(payment.getRefundNotes());
        }

        return pojo;
    }
}
