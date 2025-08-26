package timdev.timdev.controller.api.v1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import timdev.timdev.dto.LoginRequest;
import timdev.timdev.dto.api.LoginResponse;
import timdev.timdev.repository.UserRepository;
import timdev.timdev.service.AuthService;
import timdev.timdev.util.JwtUtil;

@RestController
@RequestMapping("/api/v1/auth")
// @RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        if (!response.isSuccess()) {
            if (response.getIdentifyError() != null) {
                // User not found → 404
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (response.getPasswordError() != null) {
                // Bad password → 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            // fallback bad request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // success → 200 OK
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body(
            Map.of(
                "success", true,
                "message", "Logged out successfully"
            )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required=true) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }



}