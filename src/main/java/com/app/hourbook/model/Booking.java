package com.app.hourbook.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // =========================
    // User who booked
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUserDetails user;

    // =========================
    // Ground being booked
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ground_id",
            nullable = false
    )
    private Ground ground;

    // =========================
    // Booking Date
    // =========================

    @Column(nullable = false)
    private LocalDate bookingDate;

    // =========================
    // Selected Slot
    // =========================

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // =========================
    // Price
    // =========================

    @Column(nullable = false)
    private Double pricePerHour;

    @Column(nullable = false)
    private Double totalAmount;

    // =========================
    // Booking Status
    // =========================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status =
            BookingStatus.PENDING;

    // =========================
    // Audit
    // =========================

   

@Column(name = "razorpay_order_id", unique = true)
private String razorpayOrderId;

@Column(name = "razorpay_payment_id")
private String razorpayPaymentId;

@Column(name = "razorpay_signature", length = 500)
private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; 
    
    @Column(nullable = false)
    private LocalDateTime createdAt =
            LocalDateTime.now();
    
}