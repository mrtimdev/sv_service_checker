package timdev.timdev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.User;
import timdev.timdev.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        
        // Check if username is empty
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if user is active
        if (!user.isActive()) {
            throw new DisabledException("User account is deactivated. Please contact administrator.");
        }
        
        return new CustomUserDetails(user);
    }
    
    // Optional: Add a method to load by email as well
    public CustomUserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new DisabledException("User account is deactivated. Please contact administrator.");
        }
        
        return new CustomUserDetails(user);
    }
}