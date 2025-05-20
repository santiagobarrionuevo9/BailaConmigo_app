package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EditEventRequestDto {

    private String name;

    private String description;

    private LocalDateTime dateTime;

    private String location;

    private Integer capacity;

    private String address;

    private Double price;

    private Set<DanceStyle> danceStyles;

    private String additionalInfo;
}
