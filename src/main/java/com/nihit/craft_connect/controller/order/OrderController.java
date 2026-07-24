package com.nihit.craft_connect.controller.order;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.order.OrderRequestPojo;
import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final OrderService orderService;
    private final CustomMessageSource customMessageSource;

    @PostMapping("/place")
    public ResponseEntity<GlobalApiResponse> placeOrder(
            @RequestBody OrderRequestPojo request) {

        return ResponseEntity.ok(
                successResponse(
                        "Order placed successfully!",
                        orderService.placeOrder(request)
                )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GlobalApiResponse> getOrderById(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Order"),
                        orderService.getOrderById(orderId)
                )
        );
    }

    @GetMapping("/my-orders")
    public ResponseEntity<GlobalApiResponse> getMyOrders() {

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Orders"),
                        orderService.getMyOrders()
                )
        );
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<GlobalApiResponse> cancelOrder(
            @PathVariable Long orderId) {

        orderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Order"),
                        null
                )
        );
    }

    @PutMapping("/status/{orderId}")
    public ResponseEntity<GlobalApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Order status"),
                        null
                )
        );
    }
}
