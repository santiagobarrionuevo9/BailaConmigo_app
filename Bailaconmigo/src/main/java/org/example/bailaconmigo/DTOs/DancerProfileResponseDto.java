package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;

import java.util.List;
import java.util.Set;

@Data
public class DancerProfileResponseDto {
    private Long UserId;
    private String fullName;
    private int age;
    private Long cityId;          // ID de la ciudad
    private Long countryId;       // ID del país
    private String cityName;        // Nombre de la ciudad
    private String countryName;     // Nombre del país
    private Set<DanceStyle> danceStyles;
    private Level level;
    private String aboutMe;
    private String availability;
    private List<String> mediaUrls;
    private double averageRating;
    private SubscriptionType subscriptionType;

}

