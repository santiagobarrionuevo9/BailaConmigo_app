package org.example.bailaconmigo.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.bailaconmigo.DTOs.CityDto;
import org.example.bailaconmigo.DTOs.CountryDto;
import org.example.bailaconmigo.Entities.City;
import org.example.bailaconmigo.Entities.Country;
import org.example.bailaconmigo.Repositories.CityRepository;
import org.example.bailaconmigo.Repositories.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtiene todos los países
     */
    public List<CountryDto> getAllCountries() {
        // Traemos todos los países que tengan al menos una ciudad
        List<Country> countriesWithCities = countryRepository.findAll().stream()
                .filter(country -> cityRepository.existsByCountryId(country.getId()))
                .collect(Collectors.toList());

        return countriesWithCities.stream()
                .map(this::convertToCountryDto)
                .collect(Collectors.toList());
    }


    /**
     * Obtiene ciudades por país
     */
    public List<CityDto> getCitiesByCountry(Long countryId) {
        return cityRepository.findByCountryId(countryId).stream()
                .map(this::convertToCityDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca ciudades por nombre
     */
    public List<CityDto> searchCitiesByName(String name) {
        return cityRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToCityDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca ciudades por nombre dentro de un país específico
     */
    public List<CityDto> searchCitiesByNameAndCountry(String name, Long countryId) {
        return cityRepository.findByNameContainingIgnoreCaseAndCountryId(name, countryId).stream()
                .map(this::convertToCityDto)
                .collect(Collectors.toList());
    }

    /**
     * Carga datos iniciales desde una API externa
     * Este método se puede ejecutar una vez para poblar la base de datos
     */
    public void loadInitialLocationData() {
        try {
            // Cargar países desde REST Countries API
            loadCountriesFromAPI();

            // Cargar ciudades para países específicos
            loadCitiesForImportantCountries();

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar datos de ubicación: " + e.getMessage());
        }
    }

    private void loadCountriesFromAPI() {
        try {
            String countriesUrl = "https://restcountries.com/v3.1/all?fields=name,cca2";
            String response = restTemplate.getForObject(countriesUrl, String.class);
            JsonNode countries = objectMapper.readTree(response);

            for (JsonNode countryNode : countries) {
                String countryName = countryNode.get("name").get("common").asText();
                String countryCode = countryNode.get("cca2").asText();

                // Verificar si el país ya existe
                if (!countryRepository.findByCode(countryCode).isPresent()) {
                    Country country = new Country();
                    country.setName(countryName);
                    country.setCode(countryCode);
                    countryRepository.save(country);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar países: " + e.getMessage());
        }
    }

    private void loadCitiesForImportantCountries() {
        // Argentina
        loadCitiesForArgentina();
        // España
        loadCitiesForSpain();
        // Estados Unidos
        loadCitiesForUSA();
        // México
        loadCitiesForMexico();
        // Colombia
        loadCitiesForColombia();
        // Chile
        loadCitiesForChile();
        // Perú
        loadCitiesForPeru();
        // Brasil
        loadCitiesForBrazil();
    }

    private void loadCitiesForArgentina() {
        Country argentina = countryRepository.findByCode("AR").orElse(null);
        if (argentina == null) return;

        String[] cities = {
                "Buenos Aires", "Córdoba", "Rosario", "Mendoza", "Tucumán",
                "La Plata", "Mar del Plata", "Salta", "Santa Fe", "San Juan",
                "Resistencia", "Neuquén", "Santiago del Estero", "Corrientes",
                "Posadas", "Bahía Blanca", "Paraná", "Formosa", "San Luis",
                "La Rioja", "Catamarca", "Jujuy", "Santa Rosa", "Rawson",
                "Ushuaia", "Río Gallegos", "Viedma"
        };

        addCitiesToCountry(argentina, cities);
    }

    private void loadCitiesForSpain() {
        Country spain = countryRepository.findByCode("ES").orElse(null);
        if (spain == null) return;

        String[] cities = {
                "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza",
                "Málaga", "Murcia", "Palma", "Las Palmas", "Bilbao",
                "Alicante", "Córdoba", "Valladolid", "Vigo", "Gijón",
                "Granada", "Vitoria", "A Coruña", "Elche", "Oviedo",
                "Santa Cruz de Tenerife", "Badalona", "Cartagena", "Jerez",
                "Sabadell", "Móstoles", "Alcalá de Henares", "Pamplona"
        };

        addCitiesToCountry(spain, cities);
    }

    private void loadCitiesForUSA() {
        Country usa = countryRepository.findByCode("US").orElse(null);
        if (usa == null) return;

        String[] cities = {
                "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
                "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
                "Austin", "Jacksonville", "Fort Worth", "Columbus", "Charlotte",
                "San Francisco", "Indianapolis", "Seattle", "Denver", "Washington",
                "Boston", "El Paso", "Detroit", "Nashville", "Portland",
                "Memphis", "Oklahoma City", "Las Vegas", "Louisville", "Baltimore"
        };

        addCitiesToCountry(usa, cities);
    }

    private void loadCitiesForMexico() {
        Country mexico = countryRepository.findByCode("MX").orElse(null);
        if (mexico == null) return;

        String[] cities = {
                "Ciudad de México", "Guadalajara", "Monterrey", "Puebla", "Tijuana",
                "León", "Juárez", "Torreón", "Querétaro", "San Luis Potosí",
                "Mérida", "Mexicali", "Aguascalientes", "Cuernavaca", "Saltillo",
                "Hermosillo", "Culiacán", "Morelia", "Villahermosa", "Veracruz",
                "Xalapa", "Irapuato", "Coatzacoalcos", "Mazatlán", "Tampico"
        };

        addCitiesToCountry(mexico, cities);
    }

    private void loadCitiesForColombia() {
        Country colombia = countryRepository.findByCode("CO").orElse(null);
        if (colombia == null) return;

        String[] cities = {
                "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena",
                "Cúcuta", "Bucaramanga", "Pereira", "Santa Marta", "Ibagué",
                "Soledad", "Soacha", "Pasto", "Villavicencio", "Bello",
                "Valledupar", "Montería", "Palmira", "Sincelejo", "Floridablanca"
        };

        addCitiesToCountry(colombia, cities);
    }

    private void loadCitiesForChile() {
        Country chile = countryRepository.findByCode("CL").orElse(null);
        if (chile == null) return;

        String[] cities = {
                "Santiago", "Valparaíso", "Concepción", "La Serena", "Antofagasta",
                "Temuco", "Rancagua", "Talca", "Arica", "Chillán",
                "Iquique", "Los Ángeles", "Puerto Montt", "Calama", "Copiapó",
                "Osorno", "Quillota", "Valdivia", "Punta Arenas", "San Antonio"
        };

        addCitiesToCountry(chile, cities);
    }

    private void loadCitiesForPeru() {
        Country peru = countryRepository.findByCode("PE").orElse(null);
        if (peru == null) return;

        String[] cities = {
                "Lima", "Arequipa", "Trujillo", "Chiclayo", "Piura",
                "Iquitos", "Cusco", "Chimbote", "Huancayo", "Tacna",
                "Juliaca", "Ica", "Sullana", "Ayacucho", "Chincha Alta",
                "Huánuco", "Pucallpa", "Tarapoto", "Puno", "Tumbes"
        };

        addCitiesToCountry(peru, cities);
    }

    private void loadCitiesForBrazil() {
        Country brazil = countryRepository.findByCode("BR").orElse(null);
        if (brazil == null) return;

        String[] cities = {
                "São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza",
                "Belo Horizonte", "Manaus", "Curitiba", "Recife", "Goiânia",
                "Belém", "Porto Alegre", "Guarulhos", "Campinas", "São Luís",
                "São Gonçalo", "Maceió", "Duque de Caxias", "Nova Iguaçu", "Teresina"
        };

        addCitiesToCountry(brazil, cities);
    }

    private void addCitiesToCountry(Country country, String[] cityNames) {
        for (String cityName : cityNames) {
            // Verificar si la ciudad ya existe
            List<City> existingCities = cityRepository.findByNameContainingIgnoreCaseAndCountryId(cityName, country.getId());
            if (existingCities.isEmpty()) {
                City city = new City();
                city.setName(cityName);
                city.setCountry(country);
                cityRepository.save(city);
            }
        }
    }

    // Métodos de conversión
    private CountryDto convertToCountryDto(Country country) {
        CountryDto dto = new CountryDto();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCode(country.getCode());
        return dto;
    }

    private CityDto convertToCityDto(City city) {
        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setCountryId(city.getCountry().getId());
        dto.setCountryName(city.getCountry().getName());
        dto.setCountryCode(city.getCountry().getCode());
        dto.setLatitude(city.getLatitude());
        dto.setLongitude(city.getLongitude());
        return dto;
    }

    @PostConstruct
    public void init() {
        loadInitialLocationData();
    }
}
