package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByLastPaymentReference(String referenceId);

    // Nuevos métodos para reportes
    Long countByGender(String gender);
    Long countBySubscriptionType(SubscriptionType subscriptionType);

    @Query("SELECT YEAR(u.createdDate), MONTH(u.createdDate), COUNT(u) " +
            "FROM User u " +
            "GROUP BY YEAR(u.createdDate), MONTH(u.createdDate) " +
            "ORDER BY YEAR(u.createdDate) DESC, MONTH(u.createdDate) DESC")
    List<Object[]> findMonthlyRegistrations();

    @Query("SELECT u.birthdate FROM User u WHERE u.birthdate IS NOT NULL")
    List<Object[]> findUsersWithBirthdate();

    @Query("SELECT c.name, COUNT(u) " +
            "FROM User u JOIN u.city c " +
            "GROUP BY c.name " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> findTopCities(@Param("limit") int limit);

    @Query("SELECT co.name, COUNT(u) " +
            "FROM User u JOIN u.country co " +
            "GROUP BY co.name " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> findUsersByCountry();
}

