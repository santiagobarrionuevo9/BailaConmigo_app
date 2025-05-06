package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.DancerProfileResponseDto;
import org.example.bailaconmigo.DTOs.MatchResponseDto;
import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    // 🔍 Buscar perfiles (según tipo de suscripción)
    @GetMapping("/search")
    public List<DancerProfileResponseDto> searchDancers(
            @RequestParam Long userId,
            @RequestParam String city,
            @RequestParam Set<String> styles,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String availability
    ) {
        return matchService.searchDancers(userId, city, styles, level, availability);
    }

    // ❤️ Dar like a un perfil
    @PostMapping("/like")
    public MatchResponseDto likeProfile(
            @RequestParam Long likerId,
            @RequestParam Long likedProfileId
    ) {
        return matchService.likeProfile(likerId, likedProfileId);
    }

    // 🤝 Obtener matches (donde hubo reciprocidad)
    @GetMapping("/matches")
    public List<DancerProfileResponseDto> getMatches(@RequestParam Long userId) {
        return matchService.getMatches(userId);
    }
}