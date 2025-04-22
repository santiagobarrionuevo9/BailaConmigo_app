package org.example.bailaconmigo.Services;

import org.example.bailaconmigo.DTOs.RegisterRequestDto;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param request Objeto que contiene los datos del nuevo usuario.
     * @throws RuntimeException Si el email ya está registrado o si el tipo de suscripción no es válido.
     */
    public void register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthdate(request.getBirthdate());
        user.setCity(request.getCity());

        user.setRole(request.getRole());

        if (request.getRole() == Role.BAILARIN) {
            SubscriptionType type = request.getSubscriptionType() != null ? request.getSubscriptionType() : SubscriptionType.BASICO;
            if (type != SubscriptionType.BASICO && type != SubscriptionType.PRO) {
                throw new RuntimeException("Solo BASICO o PRO para bailarines");
            }

            user.setSubscriptionType(type);
            user.setSubscriptionExpiration(type == SubscriptionType.PRO
                    ? LocalDate.now().plusMonths(3)
                    : LocalDate.now().plusDays(15));
        } else {
            user.setSubscriptionType(SubscriptionType.SIN_SUSCRIPCION);
            user.setSubscriptionExpiration(null);
        }

        userRepository.save(user);
    }


}
