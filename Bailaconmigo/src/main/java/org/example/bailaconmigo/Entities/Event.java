package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private LocalDateTime dateTime;

    private String location;

    private String address;

    private Integer capacity;

    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @ElementCollection(targetClass = DanceStyle.class)
    @CollectionTable(name = "event_styles", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "style")
    @Enumerated(EnumType.STRING)
    private Set<DanceStyle> danceStyles = new HashSet<>();

    @Column(length = 500)
    private String additionalInfo;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status = EventStatus.ACTIVO;


    // Organizador que crea el evento
    // Cambiar el tipo de User a OrganizerProfile
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerProfile organizer; // Cambiado a OrganizerProfile

    // Imágenes o media del evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventMedia> media = new ArrayList<>();

    // Calificaciones del evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRating> ratings = new ArrayList<>();

    // Método auxiliar para obtener el promedio de estrellas
    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream().mapToInt(EventRating::getStars).average().orElse(0.0);
    }


}
