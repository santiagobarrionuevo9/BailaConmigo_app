package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.EventType;
import org.example.bailaconmigo.Entities.OrganizerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByOrganizer(OrganizerProfile organizer);
    List<Event> findByEventType(EventType eventType);
}
