package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DancerProfileRepository extends JpaRepository<DancerProfile, Long> {


    // Obtener perfil de un usuario por ID
    Optional<DancerProfile> findByUser_Id(Long userId);

    // Búsqueda básica (BASICO)
    List<DancerProfile> findByCityAndDanceStylesIn(String city, Set<DanceStyle> danceStyles);

    // Búsqueda avanzada (PRO) — si querés mantener este también
    List<DancerProfile> findByCityAndDanceStylesInAndLevelAndAvailability(
            String city,
            Set<DanceStyle> danceStyles,
            Level level,
            String availability
    );

}

