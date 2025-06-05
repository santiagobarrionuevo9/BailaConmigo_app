package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.EventRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    boolean existsByRaterIdAndEventId(Long raterId, Long eventId);

    // Ratings por rango de fechas
    @Query("SELECT er FROM EventRating er WHERE er.event.dateTime BETWEEN :startDate AND :endDate")
    List<EventRating> findRatingsByEventDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Promedio de rating por tipo de evento
    @Query("SELECT e.eventType, AVG(CAST(er.stars AS double)) FROM EventRating er " +
            "JOIN er.event e GROUP BY e.eventType")
    List<Object[]> findAverageRatingByEventType();

    // Promedio de rating por estilo de baile
    @Query("SELECT ds, AVG(CAST(er.stars AS double)) FROM EventRating er " +
            "JOIN er.event e JOIN e.danceStyles ds GROUP BY ds")
    List<Object[]> findAverageRatingByDanceStyle();

    // Ratings por organizador
    @Query("SELECT o.id, o.organizationName, AVG(CAST(er.stars AS double)), COUNT(er) " +
            "FROM EventRating er JOIN er.event e JOIN e.organizer o " +
            "GROUP BY o.id, o.organizationName ORDER BY AVG(CAST(er.stars AS double)) DESC")
    List<Object[]> findRatingsByOrganizer();

    // Top eventos mejor calificados
    @Query("SELECT e.id, e.name, AVG(CAST(er.stars AS double)) as avgRating, COUNT(er) as totalRatings " +
            "FROM EventRating er JOIN er.event e " +
            "GROUP BY e.id, e.name " +
            "HAVING COUNT(er) >= :minRatings " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedEvents(@Param("minRatings") int minRatings);

    // Distribución de estrellas global
    @Query("SELECT er.stars, COUNT(er) FROM EventRating er GROUP BY er.stars ORDER BY er.stars")
    List<Object[]> findStarsDistribution();

    // Ratings con comentarios
    @Query("SELECT COUNT(er) FROM EventRating er WHERE er.comment IS NOT NULL AND er.comment != ''")
    Long countRatingsWithComments();
}
