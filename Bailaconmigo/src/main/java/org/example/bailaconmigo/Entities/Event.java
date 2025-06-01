package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;

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

    // Relación con City
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    // Relación con Country
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    // Organizador que crea el evento
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerProfile organizer;

    // Imágenes o media del evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventMedia> media = new ArrayList<>();

    // Calificaciones del evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRating> ratings = new ArrayList<>();

    // Inscripciones al evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    // Métodos helper para obtener nombres (backward compatibility)
    public String getCityName() {
        return city != null ? city.getName() : null;
    }

    public String getCountryName() {
        return country != null ? country.getName() : null;
    }

    // Métodos helper para establecer nombres (si necesitas mantener compatibilidad con código existente)
    @Deprecated
    public void setCityName(String cityName) {
        // Este método se mantiene para compatibilidad pero se recomienda usar setCity()
        // La lógica real debe manejarse en el servicio buscando la City por nombre
    }

    @Deprecated
    public void setCountryName(String countryName) {
        // Este método se mantiene para compatibilidad pero se recomienda usar setCountry()
        // La lógica real debe manejarse en el servicio buscando el Country por nombre
    }

    // Método auxiliar para obtener el promedio de estrellas
    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream().mapToInt(EventRating::getStars).average().orElse(0.0);
    }

    // Método para obtener las plazas disponibles
    public int getAvailableCapacity() {
        if (registrations == null) {
            return capacity;
        }
        // Contar solo las inscripciones confirmadas
        long confirmedRegistrations = registrations.stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.CONFIRMADO)
                .count();
        return capacity - (int) confirmedRegistrations;
    }

    // Método para verificar si hay cupo disponible
    public boolean hasAvailableCapacity() {
        return getAvailableCapacity() > 0;
    }
}
