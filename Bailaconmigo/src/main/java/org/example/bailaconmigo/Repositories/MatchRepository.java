package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.DTOs.MatchingReportDto;
import org.example.bailaconmigo.DTOs.UserMatchingStatsDto;
import org.example.bailaconmigo.Entities.Match;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {



    @Query("SELECT COUNT(m) FROM Match m WHERE m.liker.id = :userId AND m.createdDate = :today")
    long countLikesToday(Long userId, LocalDate today);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.liker.id = :userId AND m.createdDate = :today AND m.matched = true")
    long countMatchesToday(Long userId, LocalDate today);

    @Query("SELECT m FROM Match m WHERE m.liker.id = :likerId AND m.likedUser.id = :likedUserId")
    Optional<Match> findByLikerIdAndLikedUserId(Long likerId, Long likedUserId);

    @Query("SELECT m FROM Match m WHERE " +
            "(m.liker.id = :userA AND m.likedUser.id = :userB) OR " +
            "(m.liker.id = :userB AND m.likedUser.id = :userA)")
    List<Match> findMatchesBetweenUsers(Long userA, Long userB);

    @Query("SELECT m FROM Match m WHERE m.matched = true AND (m.liker.id = :userId OR m.likedUser.id = :userId)")
    List<Match> findMatchesByUserId(Long userId);

    // ===== NUEVAS QUERIES PARA REPORTES =====

    // Métricas generales
    @Query("SELECT COUNT(m) FROM Match m")
    Long countAllLikes();

    @Query("SELECT COUNT(m) FROM Match m WHERE m.matched = true")
    Long countAllMatches();

    @Query("SELECT COUNT(DISTINCT m.liker.id) FROM Match m")
    Long countDistinctLikers();

    // Métricas por período
    @Query("SELECT COUNT(m) FROM Match m WHERE m.createdDate = :date")
    Long countLikesByDate(LocalDate date);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.matched = true AND m.createdDate = :date")
    Long countMatchesByDate(LocalDate date);

    @Query("SELECT COUNT(DISTINCT m.liker.id) FROM Match m WHERE m.createdDate = :date")
    Long countActiveUsersByDate(LocalDate date);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.createdDate >= :startDate AND m.createdDate <= :endDate")
    Long countLikesByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.matched = true AND m.createdDate >= :startDate AND m.createdDate <= :endDate")
    Long countMatchesByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT m.liker.id) FROM Match m WHERE m.createdDate >= :startDate AND m.createdDate <= :endDate")
    Long countActiveUsersByDateRange(LocalDate startDate, LocalDate endDate);
    // Top usuarios más activos (para estadísticas adicionales)
    @Query("SELECT new org.example.bailaconmigo.DTOs.UserMatchingStatsDto(" +
            "m.liker.id, m.liker.fullName, COUNT(m), " +
            "SUM(CASE WHEN m.matched = true THEN 1 ELSE 0 END)) " +
            "FROM Match m " +
            "GROUP BY m.liker.id, m.liker.fullName " +
            "ORDER BY COUNT(m) DESC")
    List<UserMatchingStatsDto> findTopActiveUsers(Pageable pageable);


    // Agregar este método a tu MatchRepository
    @Query("SELECT DISTINCT CASE WHEN m.liker.id = :userId THEN m.likedUser.id ELSE m.liker.id END " +
            "FROM Match m " +
            "WHERE m.liker.id = :userId OR m.likedUser.id = :userId")
    Set<Long> findInteractedUserIds(@Param("userId") Long userId);

    @Query("SELECT m.likedUser.id FROM Match m WHERE m.liker.id = :userId")
    Set<Long> findUserIdsLikedByUser(@Param("userId") Long userId);

}
