package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.CityDto;
import org.example.bailaconmigo.DTOs.CountryDto;
import org.example.bailaconmigo.Services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * Obtiene todos los países
     */
    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        try {
            List<CountryDto> countries = locationService.getAllCountries();
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtiene ciudades por país
     */
    @GetMapping("/cities/country/{countryId}")
    public ResponseEntity<List<CityDto>> getCitiesByCountry(@PathVariable Long countryId) {
        try {
            List<CityDto> cities = locationService.getCitiesByCountry(countryId);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Busca ciudades por nombre (en todos los países)
     */
    @GetMapping("/cities/search")
    public ResponseEntity<List<CityDto>> searchCities(@RequestParam String name) {
        try {
            List<CityDto> cities = locationService.searchCitiesByName(name);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Busca ciudades por nombre dentro de un país específico
     */
    @GetMapping("/cities/search/country/{countryId}")
    public ResponseEntity<List<CityDto>> searchCitiesInCountry(
            @PathVariable Long countryId,
            @RequestParam String name) {
        try {
            List<CityDto> cities = locationService.searchCitiesByNameAndCountry(name, countryId);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para cargar datos iniciales (solo para administradores)
     * Se ejecuta una sola vez para poblar la base de datos
     */
    @PostMapping("/admin/load-initial-data")
    public ResponseEntity<String> loadInitialData() {
        try {
            locationService.loadInitialLocationData();
            return ResponseEntity.ok("Datos de ubicación cargados exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar datos: " + e.getMessage());
        }
    }
}