package com.app.hourbook.model;

import java.time.LocalDate;

import com.app.hourbook.enums.KycStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "vendor_details")
@Setter
@Getter
public class VendorDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false,unique = true)
    private String phoneNumber;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true ,nullable = false)
    private String email;
    
    private boolean isActive =true;
    
    private String otp;
    
    private boolean emailVerified;
    

    // ================= KYC =================

    @Column(length = 12)
    private String aadhaarNumber;

    private String aadhaarImage;

    @Column(length = 10)
    private String panNumber;

    private String panImage;

    private LocalDate dateOfBirth;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;
    
    
    //==========================Auth==Mapping============
    
    @OneToOne
    @JoinColumn(name = "authId",nullable = false, unique = true)
    private Vendor vendorAuth;
    
    
}