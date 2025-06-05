package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopRatedProfileDto {
    private Long profileId;
    private String profileName;
    private Level level;
    private Double averageRating;
    private Integer totalRatings;
    private List<DanceStyle> danceStyles;
}
