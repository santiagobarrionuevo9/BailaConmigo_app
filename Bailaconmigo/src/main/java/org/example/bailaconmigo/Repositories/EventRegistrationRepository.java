package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.EventRegistration;
import org.example.bailaconmigo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByEvent(Event event);

    List<EventRegistration> findByDancer(User dancer);

    Optional<EventRegistration> findByEventAndDancer(Event event, User dancer);

    boolean existsByEventAndDancer(Event event, User dancer);

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.event.id = :eventId AND er.status = org.example.bailaconmigo.Entities.Enum.RegistrationStatus.CONFIRMADO")
    int countConfirmedRegistrationsByEventId(@Param("eventId") Long eventId);
}
