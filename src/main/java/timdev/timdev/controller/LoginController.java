package timdev.timdev.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    
    private final AuthenticationManager authenticationManager;
    
    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    @GetMapping("/auth/login")
    public String showLoginForm(
        Authentication authentication,
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String logout,
        Model model,
        HttpServletRequest request) {
        
        // Check if user is already authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/admin/dashboard";
        }
        
        // For error cases handled by Spring Security
        if (error != null) {
            // Check session for specific error messages (for edge cases)
            HttpSession session = request.getSession(false);
            String errorMessage = (String) session.getAttribute("error");
            
            if (errorMessage != null) {
                model.addAttribute("error", errorMessage);
                session.removeAttribute("error"); // Clear after displaying
            } else {
                // Generic error message for Spring Security failures
                model.addAttribute("error", "Invalid username or password");
            }
        }
        
        // Handle logout message
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        return "auth/login";
    }
    
    @PostMapping("/auth/login")
    public String login(@RequestParam String username, 
                    @RequestParam String password,
                    HttpServletRequest request) {
        try {
            // Validate inputs
            if (username == null || username.trim().isEmpty()) {
                request.getSession().setAttribute("error", "Username cannot be empty");
                return "redirect:/auth/login";
            }
            
            if (password == null || password.trim().isEmpty()) {
                request.getSession().setAttribute("error", "Password cannot be empty");
                return "redirect:/auth/login";
            }
            
            // Attempt authentication
            Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username.trim(), password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Clear any previous error messages
            request.getSession().removeAttribute("error");
            
            // Redirect to dashboard
            return "redirect:/dashboard";
            
        } catch (AuthenticationException e) {
            // Store specific error message in session
            String errorMessage = getErrorMessage(e);
            request.getSession().setAttribute("error", errorMessage);
            return "redirect:/auth/login";
        }
    }
    
    private String getErrorMessage(AuthenticationException e) {
        String errorMessage = "Invalid login attempt";
        
        if (e.getMessage() != null) {
            // Check if the message contains specific information
            String message = e.getMessage().toLowerCase();
            
            if (message.contains("disabled") || message.contains("deactivated")) {
                return "Your account is deactivated. Please contact administrator.";
            } else if (message.contains("credentials") || message.contains("bad credentials")) {
                return "Invalid username or password";
            } else if (message.contains("not found")) {
                return "User not found. Please check your username/email.";
            } else if (message.contains("locked")) {
                return "Your account is locked. Please contact administrator.";
            } else if (message.contains("expired")) {
                return "Your account has expired";
            }
        }
        
        return errorMessage;
    }
    
    @GetMapping("/me")
    public String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Currently logged in: " + authentication.getName();
    }
    
    @PostMapping("/auth/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Delete remember-me cookie
        Cookie cookie = new Cookie("remember-me", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/auth/login?logout=true";
    }
}