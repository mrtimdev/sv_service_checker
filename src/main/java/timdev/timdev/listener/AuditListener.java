package timdev.timdev.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import timdev.timdev.service.AuditLogService;

import java.time.LocalDateTime;

@Component
public class AuditListener {

    private static AuditLogService staticAuditLogService;

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
    }

    @Autowired
    public void init(AuditLogService service) {
        staticAuditLogService = service;
    }

    @PrePersist
    public void prePersist(Object entity) {
        saveLog("CREATE", entity, null, toJson(entity));
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        // oldValues can be tricky to fetch, here we just leave it null for simplicity
        saveLog("UPDATE", entity, null, toJson(entity));
    }

    @PreRemove
    public void preRemove(Object entity) {
        saveLog("DELETE", entity, toJson(entity), null);
    }

    private void saveLog(String action, Object entity, String oldValues, String newValues) {
        String username = getCurrentUsername();

        staticAuditLogService.log(
                action,
                entity.getClass().getSimpleName(),
                getEntityId(entity), // fetch ID dynamically
                oldValues,
                newValues,
                username,
                null,       // ipAddress
                null,       // userAgent
                "Auto log of " + action
        );
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "system";
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }


    private Long getEntityId(Object entity) {
        try {
            var method = entity.getClass().getMethod("getId");
            Object id = method.invoke(entity);
            if (id instanceof Long) return (Long) id;
        } catch (Exception ignored) { }
        return null;
    }
}
