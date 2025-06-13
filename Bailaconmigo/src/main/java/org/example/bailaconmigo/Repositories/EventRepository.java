package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.City;
import org.example.bailaconmigo.Entities.Country;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.Enum.EventType;
import org.example.bailaconmigo.Entities.OrganizerProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByOrganizer(OrganizerProfile organizer);
    List<Event> findByEventType(EventType eventType);
    // Nuevos métodos para buscar por ubicación
    List<Event> findByCity(City city);
    List<Event> findByCountry(Country country);
    List<Event> findByCityId(Long cityId);
    List<Event> findByCountryId(Long countryId);
    List<Event> findByCityIdAndCountryId(Long cityId, Long countryId);



    @Query("SELECT e FROM Event e JOIN FETCH e.registrations WHERE e.dateTime BETWEEN :start AND :end AND e.status = :status")
    List<Event> findByDateTimeBetweenAndStatusWithOrganizer(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") EventStatus status);


}
