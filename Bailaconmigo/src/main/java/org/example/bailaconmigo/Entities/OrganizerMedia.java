package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organizer_media")
public class OrganizerMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private String type; // Ejemplo: "image", "logo", etc.

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private OrganizerProfile profile;
}
