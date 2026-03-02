package timdev.timdev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.dto.LoginRequest;
import timdev.timdev.dto.api.LoginResponse;
import timdev.timdev.entity.User;
import timdev.timdev.repository.UserRepository;
import timdev.timdev.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // Renamed from jwtService to jwtUtil
    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        var optionalUser = userRepository.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier());

        if (optionalUser.isEmpty()) {
            return LoginResponse.failure("Oops! We couldn't find that username or email. Please check and try again.", null);
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return LoginResponse.failure(null, "Uh oh! The password you entered is incorrect. Please try again.");
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getIdentifier(),
                request.getPassword()
            )
        );

        // Generate both access token and refresh token
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        return LoginResponse.success(accessToken, refreshToken, user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    // Method to refresh token
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return LoginResponse.failure("Invalid or expired refresh token", null);
        }
        
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = getByUsername(username);
        
        if (user == null) {
            return LoginResponse.failure("User not found", null);
        }
        
        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        
        // Invalidate old refresh token
        jwtUtil.invalidateToken(refreshToken);
        
        return LoginResponse.success(newAccessToken, newRefreshToken, user);
    }
}