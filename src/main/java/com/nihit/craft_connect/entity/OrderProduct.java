package com.nihit.craft_connect.entity;

import com.nihit.craft_connect.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_product")
@Entity
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price_at_purchase")
    private Double priceAtPurchase;

    @Column(name = "sub_total")
    private Double subTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status")
    private OrderStatus itemStatus;
}
