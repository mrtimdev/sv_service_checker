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
    private final JwtUtil jwtService;
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

        String token = jwtService.generateToken(user);
        return LoginResponse.success(token, user.getUsername(), user.getRole());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }


}
