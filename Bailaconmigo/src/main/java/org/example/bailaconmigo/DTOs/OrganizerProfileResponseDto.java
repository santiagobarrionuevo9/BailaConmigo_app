package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class OrganizerProfileResponseDto {
    private Long id;
    private String organizationName;
    private String contactEmail;
    private String contactPhone;
    private String description;
    private String website;
    private List<String> mediaUrls;
    private Long cityId;          // ID de la ciudad
    private Long countryId;       // ID del país
    private String cityName;        // Nombre de la ciudad
    private String countryName;     // Nombre del país
    private String fullName;        // Nombre completo del usuario asociado
}
