package timdev.timdev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import timdev.timdev.entity.User;
import timdev.timdev.entity.UserSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserAndActiveTrue(User user);
    
    List<UserSession> findByActiveTrue();
    
    Page<UserSession> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT s FROM UserSession s WHERE s.active = true AND " +
           "(LOWER(s.user.username) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.ipAddress) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.deviceType) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.browser) LIKE LOWER(CONCAT('%', :filter, '%')))")
    Page<UserSession> findByActiveTrueAndFilter(@Param("filter") String filter, Pageable pageable);
    
    Page<UserSession> findByUser(User user, Pageable pageable);
    
    Optional<UserSession> findBySessionId(String sessionId);
    
    List<UserSession> findByUser(User user);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.sessionId = :sessionId")
    void expireSession(@Param("sessionId") String sessionId);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user = :user AND s.active = true")
    void expireAllUserSessions(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.lastActivity < :cutoffTime AND s.active = true")
    void expireOldSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.active = true")
    long countActiveSessions();
    
    @Query("SELECT COUNT(DISTINCT s.user) FROM UserSession s WHERE s.active = true")
    long countActiveUsers();
}