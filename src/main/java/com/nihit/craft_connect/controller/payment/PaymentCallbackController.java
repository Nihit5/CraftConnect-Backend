package com.nihit.craft_connect.controller.payment;

import com.nihit.craft_connect.repository.OrderRepository;
import com.nihit.craft_connect.repository.PaymentRepository;
import com.nihit.craft_connect.service.payment.PaymentCallbackService;
import com.nihit.craft_connect.service.payment.impl.PaymentGatewayServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentCallbackService paymentCallbackService;

    private static final String FRONTEND_SUCCESS_URL = "http://localhost:5173/my-orders";
    private static final String FRONTEND_FAILURE_URL = "http://localhost:5173/my-orders";

    @GetMapping("/khalti/callback")
    public ResponseEntity<Void> khaltiCallback(
            @RequestParam String pidx,
            @RequestParam String purchase_order_id) {

        Long orderId;
        String redirectBase;
        try {
            orderId = paymentCallbackService.handleKhaltiCallback(purchase_order_id, pidx);
            redirectBase = FRONTEND_SUCCESS_URL;
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(FRONTEND_FAILURE_URL + "unknown"))
                    .build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectBase + orderId))
                .build();
    }

    @GetMapping("/esewa/success")
    public ResponseEntity<Void> esewaSuccessCallback(@RequestParam String data) {
        Long orderId = paymentCallbackService.handleEsewaCallback(data);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(FRONTEND_SUCCESS_URL + orderId))
                .build();
    }

    @GetMapping("/esewa/failure")
    public ResponseEntity<Void> esewaFailureCallback(@RequestParam(required = false) String data) {
        // eSewa hits this on user cancellation — we still need to mark the order failed
        Long orderId = paymentCallbackService.handleEsewaFailure(data);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(FRONTEND_FAILURE_URL + orderId))
                .build();
    }
}
