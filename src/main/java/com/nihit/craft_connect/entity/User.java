package com.nihit.craft_connect.entity;

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
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "mobile_number")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Column(name = "mobile_number", unique = true, nullable = false)
    private String mobileNumber;
    @Column(name = "password")
    private String password;
    @Column(name = "role")
    private String role;
    @Column(name = "image_path")
    private String displayPicturePath;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vendor_details_id")
    private VendorDetails vendorDetails;
}
