package com.nihit.craft_connect.controller.cart;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController extends BaseController {

    private final CartService cartService;
    private final CustomMessageSource customMessageSource;

    @PostMapping("/add")
    public ResponseEntity<GlobalApiResponse> addProductToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        cartService.addProductToCart(productId, quantity);

        return ResponseEntity.ok(
                successResponse(
                        "Product added to cart successfully!",
                        null
                )
        );
    }

    @PutMapping("/decrease")
    public ResponseEntity<GlobalApiResponse> removeOneFromCart(
            @RequestParam Long productId) {

        cartService.removeOneFromCart(productId);

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Cart"),
                        null
                )
        );
    }

    @DeleteMapping("/remove")
    public ResponseEntity<GlobalApiResponse> removeProductFromCart(
            @RequestParam Long productId) {

        cartService.removeProductFromCart(productId);

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_DELETE, "Product"),
                        null
                )
        );
    }

    @GetMapping("/my-cart")
    public ResponseEntity<GlobalApiResponse> getMyCart() {

        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Cart"),
                        cartService.getMyCart()
                )
        );
    }
}
