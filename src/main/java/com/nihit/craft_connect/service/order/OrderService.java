package com.nihit.craft_connect.service.order;

import com.nihit.craft_connect.dto.order.OrderRequestPojo;
import com.nihit.craft_connect.dto.order.OrderResponsePojo;
import com.nihit.craft_connect.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponsePojo placeOrder(OrderRequestPojo request);
    OrderResponsePojo getOrderById(Long orderId);
    List<OrderResponsePojo> getMyOrders();
    void cancelOrder(Long orderId);
    void updateOrderStatus(Long orderId, OrderStatus status);
}
