package com.nihit.craft_connect.service.cart;

import com.nihit.craft_connect.dto.cart.CartResponsePojo;
import org.springframework.stereotype.Service;

@Service
public interface CartService {
    void addProductToCart(Long productId, Integer quantity);

    void removeOneFromCart(Long productId);

    void removeProductFromCart(Long productId);

    CartResponsePojo getMyCart();
}
