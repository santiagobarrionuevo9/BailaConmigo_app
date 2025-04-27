package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Métodos personalizados si es necesario
    boolean existsByRaterIdAndProfileId(Long raterId, Long profileId);
}
