package com.nihit.craft_connect.service.order.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.order.OrderProductPojo;
import com.nihit.craft_connect.dto.order.OrderRequestPojo;
import com.nihit.craft_connect.dto.order.OrderResponsePojo;
import com.nihit.craft_connect.dto.payment.PaymentInitiationResult;
import com.nihit.craft_connect.entity.*;
import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.*;
import com.nihit.craft_connect.service.file.FileService;
import com.nihit.craft_connect.service.order.OrderService;
import com.nihit.craft_connect.service.payment.PaymentGatewayService;
import com.nihit.craft_connect.service.payment.impl.PaymentGatewayServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;
    private final UserDetailConfig userDetailConfig;
    private final CustomMessageSource customMessageSource;
    private final FileService fileService;
    private final PaymentGatewayServiceFactory paymentGatewayServiceFactory;
    private final ShippingAddressRepository shippingAddressRepository;
    @Override
    @Transactional
    public OrderResponsePojo placeOrder(OrderRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();

        if (request.getCartProductIds() == null || request.getCartProductIds().isEmpty()) {
            throw new AppException("Please select at least one product to order.");
        }
        if (request.getShippingAddressId() == null) {
            throw new AppException("Please select a shipping address.");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Cart")));
        ShippingAddress shippingAddress = shippingAddressRepository
                .findByIdAndUserId(request.getShippingAddressId(), userId)
                .orElseThrow(() -> new AppException("Selected shipping address not found."));
        List<CartProduct> selectedCartProducts =
                cartProductRepository.findByCartIdAndIdIn(cart.getId(), request.getCartProductIds());

        if (selectedCartProducts.isEmpty()) {
            throw new AppException("Selected products not found in your cart.");
        }
        if (selectedCartProducts.size() != request.getCartProductIds().size()) {
            throw new AppException("Some selected products are invalid or not in your cart.");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setShippingAddress(shippingAddress);
        order.setRecipientName(shippingAddress.getRecipientName());
        order.setMobileNumber(shippingAddress.getMobileNumber());
        order.setProvince(shippingAddress.getProvince());
        order.setDistrict(shippingAddress.getDistrict());
        order.setShippingAddressLine(shippingAddress.getAddress());
        order.setLandmark(shippingAddress.getLandmark());
        order.setAddressType(shippingAddress.getAddressType());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        order.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        order.setCreatedBy(userId);
        order.setModifiedBy(userId);

        PaymentMethod method = request.getPaymentMethod();
        List<OrderProduct> orderProducts = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartProduct cartProduct : selectedCartProducts) {
            Product product = productRepository.findById(cartProduct.getProduct().getId())
                    .orElseThrow(() -> new AppException(
                            customMessageSource.get(StringConstants.NOT_FOUND,
                                    customMessageSource.get(StringConstants.PRODUCT))));

            if (cartProduct.getQuantity() > product.getQuantity()) {
                throw new AppException("Product '" + product.getName() + "' is out of stock. Only "
                        + product.getQuantity() + " item(s) available.");
            }

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setQuantity(cartProduct.getQuantity());
            orderProduct.setPriceAtPurchase(product.getPrice());
            double subTotal = product.getPrice() * cartProduct.getQuantity();
            orderProduct.setSubTotal(subTotal);
            totalAmount += subTotal;

            orderProduct.setItemStatus(
                    OrderStatus.PENDING
            );

            product.setQuantity(product.getQuantity() - cartProduct.getQuantity());
            productRepository.save(product);

            orderProducts.add(orderProduct);
        }

        order.setOrderProducts(orderProducts);
        order.setTotalAmount(totalAmount);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);   // reuse the same variable — remove the old duplicate "PaymentMethod method = ..." line further down
        payment.setAmount(totalAmount);
        payment.setMerchantTxnId(UUID.randomUUID().toString());
        payment.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        payment.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        order.setStatus(OrderStatus.PENDING);
        payment.setStatus(PaymentStatus.PENDING);


        order.setPayment(payment);
        Order savedOrder = orderRepository.save(order);

        cartProductRepository.deleteAll(selectedCartProducts);

        OrderResponsePojo response = mapToResponse(savedOrder);

        if (method != PaymentMethod.CASH_ON_DELIVERY) {
            PaymentGatewayService gatewayService = paymentGatewayServiceFactory.getService(method);
            PaymentInitiationResult initiation = gatewayService.initiatePayment(savedOrder, payment);
            response.setPaymentRedirectUrl(initiation.getRedirectUrl());
            response.setPaymentFormFields(initiation.getFormFields());
        }

        return response;
    }
    @Override
    @Transactional(readOnly = true)
    public OrderResponsePojo getOrderById(Long orderId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Order")));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponsePojo> getMyOrders() {
        Long userId = userDetailConfig.getLoggedInUserId();
        return orderRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Order")));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new AppException("Order cannot be cancelled at this stage.");
        }

        // restock
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = orderProduct.getProduct();
            product.setQuantity(product.getQuantity() + orderProduct.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Order")));
        order.setStatus(status);
        order.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        orderRepository.save(order);
    }

    private OrderResponsePojo mapToResponse(Order order) {
        OrderResponsePojo response = new OrderResponsePojo();
        response.setOrderId(order.getId());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPayment() != null ? order.getPayment().getStatus() : null);
        response.setTotalAmount(order.getTotalAmount());
        response.setRecipientName(order.getRecipientName());
        response.setMobileNumber(order.getMobileNumber());
        response.setProvince(order.getProvince());
        response.setDistrict(order.getDistrict());
        response.setShippingAddressLine(order.getShippingAddressLine());
        response.setLandmark(order.getLandmark());
        response.setAddressType(order.getAddressType());

        List<OrderProductPojo> products = order.getOrderProducts().stream().map(op -> {
            OrderProductPojo pojo = new OrderProductPojo();
            pojo.setProductId(op.getProduct().getId());
            pojo.setName(op.getProduct().getName());
            pojo.setImagePath(fileService.extractFileName(op.getProduct().getImagePath()));
            pojo.setQuantity(op.getQuantity());
            pojo.setPriceAtPurchase(op.getPriceAtPurchase());
            pojo.setSubTotal(op.getSubTotal());
            pojo.setVendorStatus(op.getItemStatus());
            return pojo;
        }).toList();

        response.setProducts(products);
        return response;
    }
}
