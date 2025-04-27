package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.EventRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    boolean existsByRaterIdAndEventId(Long raterId, Long eventId);
}
