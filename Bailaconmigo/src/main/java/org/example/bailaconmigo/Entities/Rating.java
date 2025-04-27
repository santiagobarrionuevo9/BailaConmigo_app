package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que califica (bailarín)
    @ManyToOne
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    // Perfil de bailarín que recibe la calificación
    @ManyToOne
    @JoinColumn(name = "dancer_profile_id", nullable = false)
    private DancerProfile profile;

    @Column(nullable = false)
    private Integer stars;

    private String comment;
}