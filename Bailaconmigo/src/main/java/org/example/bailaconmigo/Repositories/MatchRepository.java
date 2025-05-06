package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
}
