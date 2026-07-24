package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedDateDesc(Long userId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
