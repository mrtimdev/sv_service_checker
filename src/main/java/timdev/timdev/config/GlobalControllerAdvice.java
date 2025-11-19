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

    @ModelAttribute("fullRequestURI")
    public String addFullRequestURI(HttpServletRequest request) {
        StringBuilder fullURL = new StringBuilder(request.getRequestURL().toString());

        String query = request.getQueryString();
        if (query != null) {
            fullURL.append("?").append(query);
        }

        return fullURL.toString();
    }

    @ModelAttribute("requestPathWithParams")
    public String addRequestPathWithParams(HttpServletRequest request) {
        String uri = request.getRequestURI();       // e.g., /company-trucks/oils-change
        String query = request.getQueryString();    // e.g., page=10&size=20

        return (query != null) ? uri + "?" + query : uri;
    }



    @ModelAttribute("currentLang")
    public String addCurrentLang(Locale locale) {
        String localeString = locale.getLanguage();
        return localeString;
    }

}
