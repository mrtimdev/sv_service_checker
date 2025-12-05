package timdev.timdev.config;

import java.util.Locale;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
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

    @ModelAttribute
    public void detectDevice(Model model, HttpServletRequest request) {

        String ua = "";
        if (request.getHeader("User-Agent") != null) {
            ua = request.getHeader("User-Agent").toLowerCase();
        }

        boolean isMobile = false;
        boolean isTablet = false;
        boolean isDesktop = false;

        // Detect Tablet (iPad / Android Tablet)
        if (ua.contains("ipad") ||
            (ua.contains("android") && !ua.contains("mobile"))) {
            isTablet = true;
        }
        // Detect Mobile phones
        else if (ua.contains("iphone") ||
                 ua.contains("android") ||
                 ua.contains("mobile") ||
                 ua.contains("opera mini") ||
                 ua.contains("iemobile") ||
                 ua.contains("blackberry")) {
            isMobile = true;
        }
        // Otherwise Desktop
        else {
            isDesktop = true;
        }

        model.addAttribute("isMobile", isMobile);
        model.addAttribute("isTablet", isTablet);
        model.addAttribute("isDesktop", isDesktop);
    }

}
