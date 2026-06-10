package com.nihit.craft_connect.entity;

import com.nihit.craft_connect.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vendor_details")
public class VendorDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "business_name")
    private String businessName;
    @Column(name = "province")
    private String province;
    @Column(name = "district")
    private String district;
    @Column(name = "address")
    private String address;
    @Column(name = "citizenship_front_image_path")
    private String citizenshipFrontImagePath;
    @Column(name = "citizenship_back_image_path")
    private String citizenshipBackImagePath;
    @Column(name = "pancard_path")
    private String pancardPath;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
