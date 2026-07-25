package com.nihit.craft_connect.entity;

import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    // our own reference sent to the gateway (transaction_uuid for esewa, purchase_order_id for khalti)
    @Column(name = "merchant_txn_id", unique = true)
    private String merchantTxnId;

    // id/token returned by the gateway (esewa's ref_id / khalti's pidx)
    @Column(name = "gateway_txn_id")
    private String gatewayTxnId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refund_notes")
    private String refundNotes;

}
