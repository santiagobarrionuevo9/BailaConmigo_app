package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Distribución de estilos de baile (cuántos perfiles hay por estilo)
    @Query("SELECT ds, COUNT(dp) " +
            "FROM DancerProfile dp " +
            "JOIN dp.danceStyles ds " +
            "GROUP BY ds " +
            "ORDER BY COUNT(dp) DESC")
    List<Object[]> findDanceStyleDistribution();

    // Distribución por nivel
    @Query("SELECT dp.level, COUNT(dp) " +
            "FROM DancerProfile dp " +
            "WHERE dp.level IS NOT NULL " +
            "GROUP BY dp.level " +
            "ORDER BY COUNT(dp) DESC")
    List<Object[]> findLevelDistribution();

    // Combinaciones más populares (usuarios con múltiples estilos)
    @Query("SELECT dp.id, SIZE(dp.danceStyles) " +
            "FROM DancerProfile dp " +
            "WHERE SIZE(dp.danceStyles) > 1 " +
            "ORDER BY SIZE(dp.danceStyles) DESC")
    List<Object[]> findProfilesWithMultipleStyles();


    // Estilos por ciudad
    @Query("SELECT dp.city, ds, COUNT(dp) " +
            "FROM DancerProfile dp " +
            "JOIN dp.danceStyles ds " +
            "WHERE dp.city IS NOT NULL " +
            "GROUP BY dp.city, ds " +
            "ORDER BY dp.city, COUNT(dp) DESC")
    List<Object[]> findDanceStylesByCity();

    // Estilos por país (usuario relacionado)
    @Query("SELECT u.country.name, ds, COUNT(dp) " +
            "FROM DancerProfile dp " +
            "JOIN dp.user u " +
            "JOIN dp.danceStyles ds " +
            "WHERE u.country IS NOT NULL " +
            "GROUP BY u.country.name, ds " +
            "ORDER BY u.country.name, COUNT(dp) DESC")
    List<Object[]> findDanceStylesByCountry();


    // Top combinaciones de estilos más comunes (usando SQL nativo) - CORREGIDO
    @Query(value = """
    SELECT combination, COUNT(*) as dancer_count
    FROM (
        SELECT dp.id,
               GROUP_CONCAT(ds.style ORDER BY ds.style SEPARATOR ', ') as combination
        FROM dancer_profiles dp 
        JOIN dancer_styles ds ON dp.id = ds.dancer_id 
        GROUP BY dp.id 
        HAVING COUNT(ds.style) > 1
    ) combinations
    GROUP BY combination
    ORDER BY dancer_count DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopStyleCombinations(@Param("limit") int limit);

    // Estadísticas por nivel y estilo
    @Query("SELECT dp.level, ds, COUNT(dp) " +
            "FROM DancerProfile dp " +
            "JOIN dp.danceStyles ds " +
            "WHERE dp.level IS NOT NULL " +
            "GROUP BY dp.level, ds " +
            "ORDER BY dp.level, COUNT(dp) DESC")
    List<Object[]> findStyleDistributionByLevel();

    // Promedio de estilos por bailarín
    @Query("SELECT AVG(SIZE(dp.danceStyles)) " +
            "FROM DancerProfile dp " +
            "WHERE SIZE(dp.danceStyles) > 0")
    Double findAverageStylesPerDancer();

    // Bailarines con estilos específicos por ciudad
    @Query("SELECT dp.city, COUNT(DISTINCT dp) " +
            "FROM DancerProfile dp " +
            "JOIN dp.danceStyles ds " +
            "WHERE ds = :style AND dp.city IS NOT NULL " +
            "GROUP BY dp.city " +
            "ORDER BY COUNT(DISTINCT dp) DESC")
    List<Object[]> findCitiesByDanceStyle(@Param("style") DanceStyle style);

    // Total de bailarines activos (con al menos un estilo)
    @Query("SELECT COUNT(dp) FROM DancerProfile dp WHERE SIZE(dp.danceStyles) > 0")
    Long countActiveDancers();


}

