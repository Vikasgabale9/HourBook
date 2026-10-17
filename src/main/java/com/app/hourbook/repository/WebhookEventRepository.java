
package com.app.hourbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.hourbook.model.WebhookEvent;

@Repository
public interface WebhookEventRepository
        extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    /*
     * MySQL atomic insert:
     * 1 = new event inserted
     * 0 = duplicate event already exists
     */
    @Modifying
    @Query(value = """
        INSERT IGNORE INTO webhook_events
            (event_id, event_type, status, received_at)
        VALUES
            (:eventId, :eventType, 'PROCESSING', CURRENT_TIMESTAMP)
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType
    );
}
