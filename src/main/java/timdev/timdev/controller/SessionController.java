package timdev.timdev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import timdev.timdev.entity.UserSession;
import timdev.timdev.service.SessionService;
import timdev.timdev.service.SessionTerminationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/sessions")
@PreAuthorize("hasRole('ADMIN')")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionRegistry sessionRegistry;
    
    @Autowired
    private SessionTerminationService sessionTerminationService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public String viewSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filter,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastActivity").descending());
        Page<UserSession> sessionPage;
        
        if (filter != null && !filter.trim().isEmpty()) {
            sessionPage = sessionService.getAllActiveSessionsPage(filter, pageable);
        } else {
            sessionPage = sessionService.getAllActiveSessionsPage(pageable);
        }
        
        model.addAttribute("sessions", sessionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessionPage.getTotalPages());
        model.addAttribute("totalItems", sessionPage.getTotalElements());
        model.addAttribute("filter", filter);
        
        // Statistics
        Map<String, Object> stats = sessionService.getSessionStats();
        model.addAttribute("stats", stats);
        
        // Device breakdown
        Map<String, Long> deviceStats = getDeviceStatistics(sessionPage.getContent());
        model.addAttribute("deviceStats", deviceStats);
        
        return "admin/sessions-table";
    }
    
    @PostMapping("/expire/{sessionId}")
    public String expireSession(@PathVariable String sessionId) {
        // 1. Expire from Spring Security SessionRegistry
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);
        if (sessionInfo != null) {
            sessionInfo.expireNow();
        }
        
        // 2. Mark as inactive in database
        sessionService.expireSession(sessionId);
        
        return "redirect:/admin/sessions?terminated=true";
    }
    
    @PostMapping("/expire-user/{userId}")
    public String expireUserSessions(@PathVariable Long userId) {
        // Get user sessions from database
        List<UserSession> userSessions = sessionService.getUserSessions(userId);
        
        // Expire each session from Spring Security
        for (UserSession session : userSessions) {
            SessionInformation sessionInfo = sessionRegistry.getSessionInformation(session.getSessionId());
            if (sessionInfo != null) {
                sessionInfo.expireNow();
            }
        }
        
        // Mark all as inactive in database
        sessionService.expireAllUserSessions(userId);
        
        return "redirect:/admin/sessions?terminatedAll=true";
    }
    
    @PostMapping("/expire-old")
    public String expireOldSessions() {
        sessionService.expireOldSessions(30);
        return "redirect:/admin/sessions?oldExpired=true";
    }
    
    @PostMapping("/expire-all")
    public String expireAllSessions() {
        // Expire all from Spring Security SessionRegistry
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                session.expireNow();
            }
        }
        
        // Mark all as inactive in database
        sessionService.expireAllSessions();
        
        return "redirect:/admin/sessions?allExpired=true";
    }
    
    @GetMapping("/details/{id}")
    @ResponseBody
    public Map<String, Object> getSessionDetails(@PathVariable Long id) {
        UserSession session = sessionService.getSessionById(id);
        Map<String, Object> details = new HashMap<>();
        
        if (session != null) {
            details.put("id", session.getId());
            details.put("username", session.getUser().getUsername());
            details.put("email", session.getUser().getEmail());
            details.put("role", session.getUser().getRole());
            details.put("sessionId", session.getSessionId());
            details.put("ipAddress", session.getIpAddress());
            details.put("deviceType", session.getDeviceType());
            details.put("browser", session.getBrowser());
            details.put("os", session.getOperatingSystem());
            details.put("userAgent", session.getUserAgent());
            details.put("loginTime", session.getLoginTime().format(formatter));
            details.put("lastActivity", session.getLastActivity().format(formatter));
            details.put("duration", formatDuration(session.getLoginTime(), session.getLastActivity()));
            details.put("active", session.isActive());
            details.put("valid", sessionTerminationService.isSessionValid(session.getSessionId()));
        }
        
        return details;
    }
    
    @PostMapping("/force-logout/{username}")
    public String forceLogoutByUsername(@PathVariable String username) {
        sessionTerminationService.forceLogoutByUsername(username);
        return "redirect:/admin/sessions?forceLoggedOut=" + username;
    }
    
    // Helper methods
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    private Map<String, Long> getDeviceStatistics(List<UserSession> sessions) {
        Map<String, Long> stats = new HashMap<>();
        
        for (UserSession session : sessions) {
            String deviceType = session.getDeviceType() != null ? session.getDeviceType() : "Unknown";
            stats.put(deviceType, stats.getOrDefault(deviceType, 0L) + 1);
        }
        
        return stats;
    }
}