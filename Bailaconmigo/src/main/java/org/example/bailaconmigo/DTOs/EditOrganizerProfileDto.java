package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.util.List;
@Data
public class EditOrganizerProfileDto {
    private String organizationName;
    private String contactEmail;
    private String contactPhone;
    private String description;
    private String website;
    private List<String> mediaUrls;
    private Long cityId;        // Agregar campo ciudad
    private Long countryId;     // Agregar campo país
}
