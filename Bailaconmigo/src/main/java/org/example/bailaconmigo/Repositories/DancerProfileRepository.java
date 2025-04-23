package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DancerProfileRepository extends JpaRepository<DancerProfile, Long> {
    Optional<DancerProfile> findByUserId(Long userId);
}

