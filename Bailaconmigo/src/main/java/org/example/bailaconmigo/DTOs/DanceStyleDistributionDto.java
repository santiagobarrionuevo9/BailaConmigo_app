package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanceStyleDistributionDto {
    private DanceStyle style;
    private String styleName;
    private Long count;
    private Double percentage;
}