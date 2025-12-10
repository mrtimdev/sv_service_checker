package timdev.timdev.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class SessionValidationFilter extends OncePerRequestFilter {
    
    @Autowired
    private SessionRegistry sessionRegistry;
    
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/auth/logout",
        "/css/",
        "/js/",
        "/images/",
        "/webjars/",
        "/error",
        "/api/v1/auth/"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String sessionId = session.getId();
            SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);
            
            // If session is explicitly expired in registry, invalidate it
            if (sessionInfo != null && sessionInfo.isExpired()) {
                session.invalidate();
                if (!request.getRequestURI().startsWith("/auth/login")) {
                    response.sendRedirect("/auth/login?expired=true");
                    return;
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
}