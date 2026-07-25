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
@Table(name = "artist_details")
public class ArtistDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "art_specialization")
    private String artSpecialization;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "citizenship_front_image_path")
    private String citizenshipFrontImagePath;

    @Column(name = "citizenship_back_image_path")
    private String citizenshipBackImagePath;

    @Column(name = "portfolio_image_path")
    private String portfolioImagePath;
}