package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;


import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dancer_profiles")
public class DancerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String fullName;

    private Integer age;

    private String city;

    @ElementCollection(targetClass = DanceStyle.class)
    @CollectionTable(name = "dancer_styles", joinColumns = @JoinColumn(name = "dancer_id"))
    @Column(name = "style")
    @Enumerated(EnumType.STRING)
    private Set<DanceStyle> danceStyles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Level level;

    @Column(length = 1000)
    private String aboutMe;

    private String availability; // ej: "Lunes a Viernes de 18 a 22", o podés usar algo más estructurado si querés

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Media> media = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    // Método auxiliar para obtener el promedio de estrellas
    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream().mapToInt(Rating::getStars).average().orElse(0.0);
    }
}
