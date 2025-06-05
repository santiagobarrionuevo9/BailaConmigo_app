package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Métodos personalizados si es necesario
    boolean existsByRaterIdAndProfileId(Long raterId, Long profileId);

    // Promedio de rating por nivel de baile
    @Query("SELECT dp.level, AVG(CAST(r.stars AS double)) FROM Rating r " +
            "JOIN r.profile dp GROUP BY dp.level")
    List<Object[]> findAverageRatingByLevel();

    // Promedio de rating por estilo de baile de perfil
    @Query("SELECT ds, AVG(CAST(r.stars AS double)) FROM Rating r " +
            "JOIN r.profile dp JOIN dp.danceStyles ds GROUP BY ds")
    List<Object[]> findAverageRatingByProfileDanceStyle();

    // Top perfiles mejor calificados
    @Query("SELECT dp.id, dp.fullName, AVG(CAST(r.stars AS double)) as avgRating, COUNT(r) as totalRatings " +
            "FROM Rating r JOIN r.profile dp " +
            "GROUP BY dp.id, dp.fullName " +
            "HAVING COUNT(r) >= :minRatings " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedProfiles(@Param("minRatings") int minRatings);

    // Distribución de estrellas para perfiles
    @Query("SELECT r.stars, COUNT(r) FROM Rating r GROUP BY r.stars ORDER BY r.stars")
    List<Object[]> findProfileStarsDistribution();

    // Ratings con comentarios para perfiles
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.comment IS NOT NULL AND r.comment != ''")
    Long countProfileRatingsWithComments();

    // Perfiles más activos en dar ratings
    @Query("SELECT r.rater.id, r.rater.fullName, COUNT(r) as ratingsGiven " +
            "FROM Rating r GROUP BY r.rater.id, r.rater.fullName " +
            "ORDER BY ratingsGiven DESC")
    List<Object[]> findMostActiveRaters();

    // Usuarios que más ratings reciben
    @Query("SELECT dp.id, dp.fullName, COUNT(r) as ratingsReceived " +
            "FROM Rating r JOIN r.profile dp " +
            "GROUP BY dp.id, dp.fullName " +
            "ORDER BY ratingsReceived DESC")
    List<Object[]>  findMostRatedProfiles();
}
