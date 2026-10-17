
package com.app.hourbook.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 150)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    // PROCESSING, PROCESSED, IGNORED, or REVIEW_REQUIRED
    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    @Column(length = 2000)
    private String errorMessage;
}
