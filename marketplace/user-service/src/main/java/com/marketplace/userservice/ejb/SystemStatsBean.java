package com.marketplace.userservice.ejb;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SINGLETON EJB - Single instance tracking system-wide statistics.
 * Only ONE instance exists for the entire application (shared state).
 * Used for tracking registrations, logins, and system metrics.
 */
@Singleton // EJB Annotation
@Startup // EJB Annotation - initialize at startup
@Component
public class SystemStatsBean {

    private final AtomicInteger totalRegistrations = new AtomicInteger(0);
    private final AtomicInteger totalLogins = new AtomicInteger(0);
    private final AtomicInteger totalProviderRegistrations = new AtomicInteger(0);
    private final AtomicInteger totalCustomerRegistrations = new AtomicInteger(0);
    private final LocalDateTime startTime;

    public SystemStatsBean() {
        this.startTime = LocalDateTime.now();
        System.out.println("[Singleton EJB] SystemStatsBean initialized at: " + startTime);
    }

    /**
     * Singleton behavior: single shared counter for all registrations
     */
    public void incrementRegistration(String role) {
        totalRegistrations.incrementAndGet();
        if ("CUSTOMER".equals(role)) {
            totalCustomerRegistrations.incrementAndGet();
        } else if ("PROVIDER".equals(role)) {
            totalProviderRegistrations.incrementAndGet();
        }
        System.out.println("[Singleton EJB] Total registrations: " + totalRegistrations.get());
    }

    /**
     * Singleton behavior: single shared login counter
     */
    public void incrementLogin() {
        totalLogins.incrementAndGet();
        System.out.println("[Singleton EJB] Total logins: " + totalLogins.get());
    }

    public int getTotalRegistrations() {
        return totalRegistrations.get();
    }

    public int getTotalLogins() {
        return totalLogins.get();
    }

    public int getTotalProviderRegistrations() {
        return totalProviderRegistrations.get();
    }

    public int getTotalCustomerRegistrations() {
        return totalCustomerRegistrations.get();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}
