package timdev.timdev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {
    
    @GetMapping("/")
    public String handleRootRequest(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/auth/login";
    }
}
