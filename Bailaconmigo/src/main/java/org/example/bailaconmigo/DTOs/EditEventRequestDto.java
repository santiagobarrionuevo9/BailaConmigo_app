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

    // Campos para ubicación
    private Long cityId;
    private Long countryId;

    // Campo deprecated para compatibilidad (opcional)
    @Deprecated
    private String location; // Mantener temporalmente para compatibilidad

    private Integer capacity;

    private String address;

    private Double price;

    private Set<DanceStyle> danceStyles;

    private String additionalInfo;
}
