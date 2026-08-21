package com.example.gymcrm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        return auth.getName();
    }

    public void checkOwnership(String targetUsername) {
        String current = getCurrentUsername();
        if (!current.equals(targetUsername)) {
            throw new AccessDeniedException(
                    "You are not allowed to access another user's data");
        }
    }
}