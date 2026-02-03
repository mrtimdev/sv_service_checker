package timdev.timdev.config;

import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import timdev.timdev.dto.CustomUserDetails;

import java.util.Optional;

import timdev.timdev.entity.User;

// @EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Configuration
public class AuditConfig {

    
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system"); // fallback
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return Optional.ofNullable(userDetails.getUsername());
            } else if (principal instanceof String username) {
                return Optional.of(username);
            }

            return Optional.of("unknown");
        };
    }

    
    @Bean
    @Primary
    public AuditorAware<User> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails.getUser()); 
            }

            return Optional.empty();
        };
    }
}
