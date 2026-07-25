package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    List<ShippingAddress> findByUserId(Long userId);
    Optional<ShippingAddress> findByIdAndUserId(Long id, Long userId);
    Optional<ShippingAddress> findByUserIdAndIsDefaultTrue(Long userId);
}
