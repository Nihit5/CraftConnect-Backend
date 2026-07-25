package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByProduct_User_IdOrderByOrder_CreatedDateDesc(Long vendorId);
    List<OrderProduct> findByProduct_IdAndProduct_User_IdOrderByOrder_CreatedDateDesc(Long productId, Long vendorId);
    Optional<OrderProduct> findByIdAndProduct_User_Id(Long orderProductId, Long vendorId);
}
