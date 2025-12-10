package timdev.timdev.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timdev.timdev.entity.User;
import timdev.timdev.entity.UserSession;
import timdev.timdev.repository.UserSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class SessionService {
    
    @Autowired
    private UserSessionRepository sessionRepository;
    @Autowired private UserService userService;
    
    @Transactional
    public UserSession createSession(User user, String sessionId, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionId(sessionId);
        session.setIpAddress(getClientIp(request));
        
        String userAgent = request.getHeader("User-Agent");
        session.setUserAgent(userAgent);
        
        // Parse user agent for basic device info
        if (userAgent != null) {
            session.setDeviceType(detectDeviceType(userAgent));
            session.setBrowser(detectBrowser(userAgent));
            session.setOperatingSystem(detectOS(userAgent));
        }
        
        return sessionRepository.save(session);
    }
    
    @Transactional
    public void updateLastActivity(String sessionId) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        sessionOpt.ifPresent(session -> {
            session.setLastActivity(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }
    
    @Transactional
    public void expireSession(String sessionId) {
        sessionRepository.expireSession(sessionId);
    }
    
    @Transactional
    public void expireAllUserSessions(User user) {
        sessionRepository.expireAllUserSessions(user);
    }
    
    @Transactional
    public void expireOldSessions(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        sessionRepository.expireOldSessions(cutoff);
    }
    
    public List<UserSession> getUserActiveSessions(User user) {
        return sessionRepository.findByUserAndActiveTrue(user);
    }
    
    public List<UserSession> getAllActiveSessions() {
        return sessionRepository.findByActiveTrue();
    }
    

    
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim(); // Take first IP in case of multiple
            }
        }
        
        return request.getRemoteAddr();
    }
    
    private String detectDeviceType(String userAgent) {
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
    
    private String detectBrowser(String userAgent) {
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("opera")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
    
    private String detectOS(String userAgent) {
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os") || userAgent.contains("macos")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }


   public Page<UserSession> getAllActiveSessionsPage(Pageable pageable) {
        return sessionRepository.findByActiveTrue(pageable);
    }
    
    public Page<UserSession> getAllActiveSessionsPage(String filter, Pageable pageable) {
        return sessionRepository.findByActiveTrueAndFilter(filter, pageable);
    }
    
    public Page<UserSession> getUserSessionsPage(Long userId, Pageable pageable) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return Page.empty();
        }
        return sessionRepository.findByUser(user, pageable);
    }
    
    public UserSession getSessionById(Long id) {
        return sessionRepository.findById(id).orElse(null);
    }
    
    public Map<String, Object> getSessionStats() {
        long totalActiveSessions = sessionRepository.countActiveSessions();
        long totalActiveUsers = sessionRepository.countActiveUsers();
        long totalSessions = sessionRepository.count();
        
        return Map.of(
            "totalActiveSessions", totalActiveSessions,
            "totalActiveUsers", totalActiveUsers,
            "totalSessions", totalSessions
        );
    }
    
    public Map<String, Long> getDeviceStatistics() {
        List<UserSession> sessions = sessionRepository.findByActiveTrue();
        return getDeviceStatsFromSessions(sessions);
    }
    
    public Map<String, Long> getBrowserStatistics() {
        List<UserSession> sessions = sessionRepository.findByActiveTrue();
        return getBrowserStatsFromSessions(sessions);
    }
    
    private Map<String, Long> getDeviceStatsFromSessions(List<UserSession> sessions) {
        Map<String, Long> stats = new java.util.HashMap<>();
        
        for (UserSession session : sessions) {
            String deviceType = session.getDeviceType() != null ? session.getDeviceType() : "Unknown";
            stats.put(deviceType, stats.getOrDefault(deviceType, 0L) + 1);
        }
        
        return stats;
    }
    
    private Map<String, Long> getBrowserStatsFromSessions(List<UserSession> sessions) {
        Map<String, Long> stats = new java.util.HashMap<>();
        
        for (UserSession session : sessions) {
            String browser = session.getBrowser() != null ? session.getBrowser() : "Unknown";
            stats.put(browser, stats.getOrDefault(browser, 0L) + 1);
        }
        
        return stats;
    }
    
    @Transactional
    public void expireAllSessions() {
        List<UserSession> sessions = sessionRepository.findByActiveTrue();
        for (UserSession session : sessions) {
            session.setActive(false);
        }
        sessionRepository.saveAll(sessions);
    }
    
    @Transactional
    public void expireAllUserSessions(Long userId) {
        User user = userService.findById(userId).orElse(null);
        if (user != null) {
            List<UserSession> sessions = sessionRepository.findByUserAndActiveTrue(user);
            for (UserSession session : sessions) {
                session.setActive(false);
            }
            sessionRepository.saveAll(sessions);
        }
    }
    
    public long getActiveSessionCount() {
        return sessionRepository.countActiveSessions();
    }

    /**
     * Get all sessions for a specific user (both active and inactive)
     */
    public List<UserSession> getUserSessions(Long userId) {
        User user = userService.findById(userId).orElse(null);
        if (user != null) {
            return sessionRepository.findByUser(user);
        }
        return List.of(); // Return empty list if user not found
    }
    
    /**
     * Get all ACTIVE sessions for a specific user
     */
    public List<UserSession> getUserActiveSessions(Long userId) {
        User user = userService.findById(userId).orElse(null);
        if (user != null) {
            return sessionRepository.findByUserAndActiveTrue(user);
        }
        return List.of();
    }
}