package com.nihit.craft_connect.service.cart.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.cart.CartProductPojo;
import com.nihit.craft_connect.dto.cart.CartResponsePojo;
import com.nihit.craft_connect.dto.cart.VendorCartGroupPojo;
import com.nihit.craft_connect.entity.Cart;
import com.nihit.craft_connect.entity.CartProduct;
import com.nihit.craft_connect.entity.Product;
import com.nihit.craft_connect.entity.User;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        customMessageSource.get(StringConstants.NOT_FOUND, "Cart")));

        List<CartProduct> cartProducts = cartProductRepository.findByCartId(cart.getId());

        Map<User, List<CartProduct>> byVendor = cartProducts.stream()
                .collect(Collectors.groupingBy(
                        cp -> cp.getProduct().getUser(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<VendorCartGroupPojo> vendorGroups = new ArrayList<>();
        int totalItems = 0;
        double totalPrice = 0.0;

        for (Map.Entry<User, List<CartProduct>> entry : byVendor.entrySet()) {
            User vendor = entry.getKey();
            List<CartProduct> vendorCartProducts = entry.getValue();

            List<CartProductPojo> productPojos = new ArrayList<>();
            int vendorItemCount = 0;
            double vendorSubTotal = 0.0;

            for (CartProduct cartProduct : vendorCartProducts) {
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

                vendorItemCount += cartProduct.getQuantity();
                vendorSubTotal += subTotal;
                productPojos.add(pojo);
            }

            VendorCartGroupPojo group = new VendorCartGroupPojo();
            group.setVendorId(vendor.getId());
            group.setVendorName(
                    vendor.getVendorDetails() != null
                            ? vendor.getVendorDetails().getBusinessName()
                            : vendor.getFirstName() + " " + vendor.getLastName()
            );
            group.setProducts(productPojos);
            group.setItemCount(vendorItemCount);
            group.setSubTotal(vendorSubTotal);

            vendorGroups.add(group);

            totalItems += vendorItemCount;
            totalPrice += vendorSubTotal;
        }

        CartResponsePojo response = new CartResponsePojo();
        response.setCartId(cart.getId());
        response.setVendorGroups(vendorGroups);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);
        response.setProductCount((long) cartProducts.size());
        return response;
    }
}
