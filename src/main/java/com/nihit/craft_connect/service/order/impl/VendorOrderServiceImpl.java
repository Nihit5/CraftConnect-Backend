package com.nihit.craft_connect.service.order.impl;

import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.dto.order.VendorOrderItemPojo;
import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.OrderProduct;
import com.nihit.craft_connect.entity.Payment;
import com.nihit.craft_connect.entity.Product;
import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.OrderProductRepository;
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

    private static final Set<OrderStatus> VENDOR_SETTABLE_STATUSES =
            Set.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private static final List<OrderStatus> FORWARD_SEQUENCE = List.of(
            OrderStatus.PENDING_PAYMENT, OrderStatus.CONFIRMED,
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
            throw new AppException("Invalid status. Allowed: PROCESSING, SHIPPED, DELIVERED, CANCELLED.");
        }

        OrderProduct orderProduct = orderProductRepository
                .findByIdAndProduct_User_Id(orderProductId, vendorId)
                .orElseThrow(() -> new AppException("Order item not found or does not belong to you."));

        OrderStatus current = orderProduct.getItemStatus();

        if (current == null || current == OrderStatus.PENDING_PAYMENT || current == OrderStatus.PAYMENT_FAILED) {
            throw new AppException("Cannot update delivery status until payment is confirmed.");
        }
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new AppException("This item's status is already final and cannot be changed.");
        }
        if (newStatus != OrderStatus.CANCELLED && !isForwardMove(current, newStatus)) {
            throw new AppException("Cannot move status from " + current + " to " + newStatus + ".");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            cancelItem(orderProduct, cancellationReason);
        } else {
            orderProduct.setItemStatus(newStatus);
            orderProductRepository.save(orderProduct);
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
            // no money was ever collected — nothing to refund
            log.info("Item {} cancelled (COD, no refund needed)", orderProduct.getId());
        } else if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            // real money was collected via Khalti/eSewa — neither exposes a public refund API,
            // so this MUST be actioned manually through the merchant dashboard. We only record
            // that a refund is owed; we never claim it's been processed.
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

        // 3. recalc order-level status if every item ended up cancelled
        boolean allCancelled = order.getOrderProducts().stream()
                .allMatch(op -> op.getItemStatus() == OrderStatus.CANCELLED);
        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        }
    }

    private boolean isForwardMove(OrderStatus current, OrderStatus next) {
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
}
