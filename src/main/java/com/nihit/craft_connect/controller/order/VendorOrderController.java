package com.nihit.craft_connect.controller.order;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.order.VendorItemStatusUpdatePojo;
import com.nihit.craft_connect.service.order.VendorOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendor/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorOrderController extends BaseController {

    private final VendorOrderService vendorOrderService;
    private final CustomMessageSource customMessageSource;

    @GetMapping("/items")
    public ResponseEntity<GlobalApiResponse> getMyOrderItems() {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Orders"),
                vendorOrderService.getMyOrderItems()));
    }

    @GetMapping("/items/product/{productId}")
    public ResponseEntity<GlobalApiResponse> getMyOrderItemsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Orders"),
                vendorOrderService.getMyOrderItemsByProduct(productId)));
    }

    @PutMapping("/item/{orderProductId}/status")
    public ResponseEntity<GlobalApiResponse> updateItemStatus(
            @PathVariable Long orderProductId,
            @RequestBody VendorItemStatusUpdatePojo request) {
        vendorOrderService.updateItemStatus(orderProductId, request.getStatus(), request.getCancellationReason());
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Delivery status"), null));
    }

    @GetMapping("/payments")
    public ResponseEntity<GlobalApiResponse> getMyPaymentDetails() {
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Payment details"),
                        vendorOrderService.getMyPaymentDetails()
                )
        );
    }
}
