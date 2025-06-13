package org.example.bailaconmigo.Repositories;

import org.example.bailaconmigo.Entities.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryId(Long countryId);

    boolean existsByCountryId(Long countryId);


    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<City> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.country.id = :countryId")
    List<City> findByNameContainingIgnoreCaseAndCountryId(@Param("name") String name, @Param("countryId") Long countryId);
}
