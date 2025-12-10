package timdev.timdev.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import timdev.timdev.entity.User;
import timdev.timdev.entity.UserSession;
import timdev.timdev.repository.UserRepository;
import timdev.timdev.repository.UserSessionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionTerminationService {
    
    @Autowired
    private SessionRegistry sessionRegistry;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionService sessionService;
    
    /**
     * Terminate a specific session by session ID
     */
    @Transactional
    public void terminateSession(String sessionId) {
        // 1. Expire from Spring Security SessionRegistry
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);
        if (sessionInfo != null) {
            sessionInfo.expireNow();
        }
        
        // 2. Mark as inactive in database
        sessionService.expireSession(sessionId);
        
        // 3. If session is still active, invalidate it
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession currentSession = attr.getRequest().getSession(false);
            if (currentSession != null && currentSession.getId().equals(sessionId)) {
                currentSession.invalidate();
            }
        } catch (IllegalStateException e) {
            // Session already invalidated
        }
    }
    
    /**
     * Terminate ALL sessions for a specific user
     */
    @Transactional
    public void terminateAllUserSessions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // 1. Find all active sessions in database
            List<UserSession> userSessions = userSessionRepository.findByUserAndActiveTrue(user);
            
            // 2. Expire from Spring Security SessionRegistry
            for (Object principal : sessionRegistry.getAllPrincipals()) {
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                    if (userDetails.getUsername().equals(user.getUsername())) {
                        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                        for (SessionInformation session : sessions) {
                            session.expireNow();
                        }
                    }
                }
            }
            
            // 3. Mark all as inactive in database
            for (UserSession session : userSessions) {
                session.setActive(false);
                session.setLastActivity(LocalDateTime.now());
            }
            userSessionRepository.saveAll(userSessions);
            
            // 4. Invalidate current session if it belongs to this user
            try {
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpSession currentSession = attr.getRequest().getSession(false);
                if (currentSession != null) {
                    String currentUsername = attr.getRequest().getUserPrincipal() != null ? 
                        attr.getRequest().getUserPrincipal().getName() : null;
                    if (user.getUsername().equals(currentUsername)) {
                        currentSession.invalidate();
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    /**
     * Terminate ALL active sessions (admin function)
     */
    @Transactional
    public void terminateAllSessions() {
        // 1. Expire all from Spring Security SessionRegistry
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                session.expireNow();
            }
        }
        
        // 2. Mark all as inactive in database
        List<UserSession> allActiveSessions = userSessionRepository.findByActiveTrue();
        for (UserSession session : allActiveSessions) {
            session.setActive(false);
            session.setLastActivity(LocalDateTime.now());
        }
        userSessionRepository.saveAll(allActiveSessions);
        
        // 3. Invalidate current session
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession currentSession = attr.getRequest().getSession(false);
            if (currentSession != null) {
                currentSession.invalidate();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Check if a session is still valid
     */
    public boolean isSessionValid(String sessionId) {
        // Check in Spring Security registry
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);
        if (sessionInfo != null && sessionInfo.isExpired()) {
            return false;
        }
        
        // Check in database
        return userSessionRepository.findBySessionId(sessionId)
            .map(UserSession::isActive)
            .orElse(false);
    }
    
    /**
     * Force logout a user by username
     */
    @Transactional
    public void forceLogoutByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            terminateAllUserSessions(user.getId());
        }
    }
}