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

    

    @PostMapping("/auth/login")
    public String login(@RequestParam String username, 
                    @RequestParam String password,
                    HttpServletRequest request) {
        try {
            Authentication authentication = this.authenticationManager.authenticate(
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