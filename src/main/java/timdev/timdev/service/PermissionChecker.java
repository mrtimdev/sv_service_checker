package timdev.timdev.service;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class PermissionChecker {

    public boolean has(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }

    public boolean hasAny(String... permissions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> Arrays.asList(permissions).contains(a.getAuthority()));
    }

    public boolean hasRole(String role) {
        return has("ROLE_" + role);
    }
}
