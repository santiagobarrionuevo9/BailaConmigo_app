package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;

import java.util.List;
import java.util.Map;

@Data
public class ProfileRatingDetailDto {
    private Long profileId;
    private String profileName;
    private List<DanceStyle> danceStyles;
    private Level level;
    private Integer totalRatings;
    private Double averageRating;
    private Map<Integer, Long> starsDistribution;
    private List<RatingCommentDto> recentComments;
}
