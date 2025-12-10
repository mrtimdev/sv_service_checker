package timdev.timdev.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String sessionId;
    
    @Column(nullable = false)
    private String ipAddress;
    
    private String userAgent;
    
    private String deviceType; // Desktop, Mobile, Tablet
    
    private String browser;
    
    private String operatingSystem;
    
    private String location; // Could be derived from IP
    
    @Column(nullable = false)
    private LocalDateTime loginTime;
    
    private LocalDateTime lastActivity;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        loginTime = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
    }
}