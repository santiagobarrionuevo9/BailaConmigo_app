package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanceStylesByRegionDto {
    private String regionName;
    private String regionType; // "city" o "country"
    private List<DanceStyleDistributionDto> topStyles;
    private Long totalUsers;
}
