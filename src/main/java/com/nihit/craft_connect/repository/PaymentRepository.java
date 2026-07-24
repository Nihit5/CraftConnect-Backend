package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByMerchantTxnId(String merchantTxnId);
}
