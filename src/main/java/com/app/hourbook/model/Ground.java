package com.app.hourbook.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.app.hourbook.enums.Amenities;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.enums.GroundType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ground_details")
@Getter
@Setter
@NoArgsConstructor
public class Ground {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double pricePerHour;

    /*
     * Ground operating hours
     */
   // @Column(nullable = false)
    private LocalTime openingTime;

  //  @Column(nullable = false)
    private LocalTime closingTime;

    /*
     * Version 1:
     * Fixed 60-minute booking slots
     */
  //  @Column(nullable = false)
    private Integer slotDurationMinutes = 60;

    @Column(nullable = false)
    private String sports;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroundType type;

    @ElementCollection(targetClass = Amenities.class)
    @CollectionTable(
            name = "ground_amenities",
            joinColumns = @JoinColumn(name = "ground_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    private List<Amenities> amenities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroundStatus status = GroundStatus.PENDING;

    @ElementCollection
    @CollectionTable(
            name = "ground_images",
            joinColumns = @JoinColumn(name = "ground_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
}