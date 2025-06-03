package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMatchingStatsDto {
    private Long userId;
    private String fullName;
    private Long totalLikes;
    private Long totalMatches;
    private Double conversionRate;

    public UserMatchingStatsDto(Long userId, String fullName, Long totalLikes, Long totalMatches) {
        this.userId = userId;
        this.fullName = fullName;
        this.totalLikes = totalLikes;
        this.totalMatches = totalMatches;
        this.conversionRate = 0.0; // lo calculas luego en Java
    }

}
