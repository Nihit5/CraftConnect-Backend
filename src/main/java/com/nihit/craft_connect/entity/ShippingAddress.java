package com.nihit.craft_connect.entity;

import com.nihit.craft_connect.enums.AddressType;
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
@Table(name = "shipping_address")
@Entity
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "address")
    private String address;

    @Column(name = "landmark")
    private String landmark;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType;

    @Column(name = "address_label")
    private String addressLabel;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}
