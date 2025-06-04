package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedGeographicDto {
    private String name;
    private Long userCount;
    private Long dancerCount;
    private Double percentage;
    private List<DanceStyleDistributionDto> popularStyles;
}
