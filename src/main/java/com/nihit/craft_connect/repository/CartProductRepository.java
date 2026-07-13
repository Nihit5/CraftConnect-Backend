package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartProduct> findByCartId(Long cartId);
}
