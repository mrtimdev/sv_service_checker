package timdev.timdev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {
    @GetMapping("/auth/login")
    public String showLoginForm(
        Authentication authentication,
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String logout,
        Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/admin/dashboard";
        }
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/auth/login")
    public String login(@RequestParam String username, 
                    @RequestParam String password,
                    HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Redirect to a secure page after login
            return "redirect:/dashboard";
        } catch (AuthenticationException e) {
            // Add error message and return to login page
            request.getSession().setAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }

    @GetMapping("/me")
    public String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Currently logged in: " + authentication.getName();
    }
}