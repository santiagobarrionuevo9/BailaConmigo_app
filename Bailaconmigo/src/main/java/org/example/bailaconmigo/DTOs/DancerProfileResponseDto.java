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
    private String city;
    private Set<DanceStyle> danceStyles;
    private Level level;
    private String aboutMe;
    private String availability;
    private List<String> mediaUrls;
    private double averageRating;
    private SubscriptionType subscriptionType;

}

