package org.example.bailaconmigo.DTOs;

import lombok.Data;

@Data
public class CityDto {
    private Long id;
    private String name;
    private Long countryId;
    private String countryName;
    private String countryCode;
    private Double latitude;
    private Double longitude;
}