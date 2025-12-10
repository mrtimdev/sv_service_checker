package timdev.timdev.listener;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import timdev.timdev.entity.User;
import timdev.timdev.repository.UserRepository;
import timdev.timdev.service.SessionService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionTracker implements HttpSessionListener {

    @Autowired @Lazy
    private SessionService sessionService;

    @Autowired @Lazy
    private UserRepository userRepository;

    // Store last activity timestamps
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // nothing for now
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        sessionService.expireSession(sessionId);
        lastActivityMap.remove(sessionId);
    }

    public void trackLogin(String username, String sessionId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            HttpServletRequest request = 
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

            sessionService.createSession(userOpt.get(), sessionId, request);
            lastActivityMap.put(sessionId, System.currentTimeMillis());
        }
    }

    public void updateActivity(String sessionId) {
        lastActivityMap.put(sessionId, System.currentTimeMillis());
    }

    public boolean isInactive(String sessionId, long timeoutMillis) {
        Long last = lastActivityMap.get(sessionId);
        if (last == null) return true;
        return (System.currentTimeMillis() - last) > timeoutMillis;
    }

    public void removeSession(String sessionId) {
        lastActivityMap.remove(sessionId);
        sessionService.expireSession(sessionId);
    }
}
