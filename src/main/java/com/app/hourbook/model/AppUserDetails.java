package com.app.hourbook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "app_user_details")
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean isActive = true;

    private String otp;

    private boolean emailVerified;

    /*
     * User profile image URL returned by Cloudinary.
     */
    private String profileImage;

    @OneToOne
    @JoinColumn(name = "authId", nullable = false, unique = true)
    private AppUser appUser;
}