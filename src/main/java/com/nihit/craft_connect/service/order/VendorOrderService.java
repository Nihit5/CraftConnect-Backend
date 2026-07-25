package com.nihit.craft_connect.service.order;

import com.nihit.craft_connect.dto.order.VendorOrderItemPojo;
import com.nihit.craft_connect.dto.order.VendorPaymentDetailPojo;
import com.nihit.craft_connect.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VendorOrderService {
    List<VendorOrderItemPojo> getMyOrderItems();
    List<VendorOrderItemPojo> getMyOrderItemsByProduct(Long productId);
    void updateItemStatus(Long orderProductId, OrderStatus newStatus, String cancellationReason);
    List<VendorPaymentDetailPojo> getMyPaymentDetails();
}
