package org.example.bailaconmigo.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Ejemplo en Spring Boot
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200","https://3a7a-152-171-81-105.ngrok-free.app") // Tu origen Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permitir acceder a los archivos desde http://localhost:8080/uploads/...
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}