package timdev.timdev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String handleRootRequest(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                    return "redirect:/admin/service-checkers";
            }   
            return "redirect:/admin/dashboard";
        }
        return "redirect:/auth/login";
    }

}
