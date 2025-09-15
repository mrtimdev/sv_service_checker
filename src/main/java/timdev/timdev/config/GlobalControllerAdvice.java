package timdev.timdev.config;

import java.util.Locale;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import timdev.timdev.dto.CustomUserDetails;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @ModelAttribute("currentUser")
    public UserDetails getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails;
    }

    @ModelAttribute("requestURI")
    public String addRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentLang")
    public String addCurrentLang(Locale locale) {
        String localeString = locale.getLanguage();
        return localeString;
    }

}
