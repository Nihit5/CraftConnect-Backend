package com.nihit.craft_connect.service.cart.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.cart.CartProductPojo;
import com.nihit.craft_connect.dto.cart.CartResponsePojo;
import com.nihit.craft_connect.entity.Cart;
import com.nihit.craft_connect.entity.CartProduct;
import com.nihit.craft_connect.entity.Product;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.CartProductRepository;
import com.nihit.craft_connect.repository.CartRepository;
import com.nihit.craft_connect.repository.ProductRepository;
import com.nihit.craft_connect.service.cart.CartService;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserDetailConfig userDetailConfig;
    private final CustomMessageSource customMessageSource;
    private final ProductRepository productRepository;
    private final CartProductRepository cartProductRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public void addProductToCart(Long productId, Integer quantity) {

        Long userId = userDetailConfig.getLoggedInUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Cart"
                        )
                ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                customMessageSource.get(StringConstants.PRODUCT)
                        )
                ));

        CartProduct cartProduct = cartProductRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        int requestedQuantity = quantity;

        if (cartProduct != null) {
            requestedQuantity += cartProduct.getQuantity();
        }

        if (requestedQuantity > product.getQuantity()) {
            throw new AppException("Product is out of stock. Only " +
                    product.getQuantity() + " item(s) available.");
        }

        if (cartProduct == null) {

            cartProduct = new CartProduct();
            cartProduct.setCart(cart);
            cartProduct.setProduct(product);
            cartProduct.setQuantity(quantity);

        } else {

            cartProduct.setQuantity(requestedQuantity);

        }

        cartProductRepository.save(cartProduct);
    }

    @Override
    @Transactional
    public void removeOneFromCart(Long productId) {

        Long userId = userDetailConfig.getLoggedInUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Cart"
                        )
                ));

        CartProduct cartProduct = cartProductRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Product in Cart"
                        )
                ));

        if (cartProduct.getQuantity() > 1) {
            cartProduct.setQuantity(cartProduct.getQuantity() - 1);
            cartProductRepository.save(cartProduct);
        } else {
            cartProductRepository.delete(cartProduct);
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long productId) {

        Long userId = userDetailConfig.getLoggedInUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Cart"
                        )
                ));

        CartProduct cartProduct = cartProductRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Product in Cart"
                        )
                ));

        cartProductRepository.delete(cartProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponsePojo getMyCart() {

        Long userId = userDetailConfig.getLoggedInUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "Cart"
                        )
                ));

        List<CartProduct> cartProducts =
                cartProductRepository.findByCartId(cart.getId());

        CartResponsePojo response = new CartResponsePojo();

        response.setCartId(cart.getId());

        List<CartProductPojo> productPojos = new ArrayList<>();

        int totalItems = 0;
        double totalPrice = 0.0;

        for (CartProduct cartProduct : cartProducts) {

            Product product = cartProduct.getProduct();

            CartProductPojo pojo = new CartProductPojo();

            pojo.setProductId(product.getId());
            pojo.setName(product.getName());
            pojo.setDescription(product.getDescription());
            pojo.setImagePath(fileService.extractFileName(product.getImagePath()));
            pojo.setPrice(product.getPrice());
            pojo.setCartProductId(cartProduct.getId());

            pojo.setAvailableQuantity(product.getQuantity());

            pojo.setCartQuantity(cartProduct.getQuantity());

            double subTotal = product.getPrice() * cartProduct.getQuantity();

            pojo.setSubTotal(subTotal);

            totalItems += cartProduct.getQuantity();
            totalPrice += subTotal;

            productPojos.add(pojo);
        }

        response.setProducts(productPojos);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);

        return response;
    }
}
