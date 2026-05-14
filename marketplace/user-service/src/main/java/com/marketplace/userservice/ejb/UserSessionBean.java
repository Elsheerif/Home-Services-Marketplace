package com.marketplace.userservice.ejb;

import com.marketplace.userservice.entity.User;
import jakarta.ejb.Stateful;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * STATEFUL EJB - Manages user login sessions.
 * Each session has its own state (logged-in user info).
 * Simulated as a Spring @Component with session-like scope using a shared session map.
 */
@Stateful  // EJB Annotation
@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class UserSessionBean {

    // Simulates per-session state: sessionToken -> User
    private final Map<String, User> activeSessions = new HashMap<>();

    /**
     * Stateful behavior: creates a session for a logged-in user
     */
    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);
        System.out.println("[Stateful EJB] Session created for user: " + user.getUsername() + " | Token: " + token);
        return token;
    }

    /**
     * Stateful behavior: retrieves user from their session state
     */
    public User getUserFromSession(String token) {
        return activeSessions.get(token);
    }

    /**
     * Stateful behavior: invalidates/removes session (logout)
     */
    public void removeSession(String token) {
        User user = activeSessions.remove(token);
        if (user != null) {
            System.out.println("[Stateful EJB] Session removed for user: " + user.getUsername());
        }
    }

    public boolean isSessionValid(String token) {
        return activeSessions.containsKey(token);
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
