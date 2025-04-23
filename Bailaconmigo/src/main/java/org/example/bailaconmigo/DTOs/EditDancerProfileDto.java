package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;

import java.util.List;
import java.util.Set;

@Data
public class EditDancerProfileDto {

    private String city;

    private Set<DanceStyle> danceStyles;

    private Level level;

    private String aboutMe;

    private String availability;

    private List<String> mediaUrls; // URLs de imágenes/videos (simulación)
}

