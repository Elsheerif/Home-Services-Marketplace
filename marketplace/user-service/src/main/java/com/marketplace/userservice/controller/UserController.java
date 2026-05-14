package com.marketplace.userservice.controller;

import com.marketplace.userservice.dto.*;
import com.marketplace.userservice.service.NotificationService;
import com.marketplace.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    // ==================== REGISTRATION ====================

    /** FR Customer 1 & 2: Register as customer with initial balance */
    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterCustomerRequest req) {
        try {
            UserResponse response = userService.registerCustomer(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** FR Provider 4: Register as service provider with profession type */
    @PostMapping("/register/provider")
    public ResponseEntity<?> registerProvider(@RequestBody RegisterProviderRequest req) {
        try {
            UserResponse response = userService.registerProvider(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== AUTHENTICATION ====================

    /** FR Provider 5 / Customer login: Login returns session token (Stateful EJB) */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            LoginResponse response = userService.login(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Logout: invalidates session (Stateful EJB removes session state) */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.logout(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== WALLET ====================

    /** FR Customer 3: Add funds to wallet */
    @PostMapping("/{userId}/wallet/add")
    public ResponseEntity<?> addFunds(@PathVariable Long userId,
                                       @RequestBody AddFundsRequest req) {
        try {
            return ResponseEntity.ok(userService.addFunds(userId, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** FR Customer 4: View wallet balance */
    @GetMapping("/{userId}/wallet")
    public ResponseEntity<?> getWallet(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.getWalletBalance(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== INTERNAL APIs (called by other microservices) ====================

    /** Internal: Get user by ID (used by booking-service) */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Internal: Deduct wallet (used by booking-service) */
    @PostMapping("/{userId}/wallet/deduct")
    public ResponseEntity<?> deductWallet(@PathVariable Long userId,
                                           @RequestBody Map<String, Double> body) {
        try {
            Double amount = body.get("amount");
            return ResponseEntity.ok(userService.deductWallet(userId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Internal: Refund wallet (used by booking-service on rollback) */
    @PostMapping("/{userId}/wallet/refund")
    public ResponseEntity<?> refundWallet(@PathVariable Long userId,
                                           @RequestBody Map<String, Double> body) {
        try {
            Double amount = body.get("amount");
            return ResponseEntity.ok(userService.refundWallet(userId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ADMIN ====================

    /** FR Admin 1: View all registered users */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** System stats from Singleton EJB */
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(userService.getSystemStats());
    }

    // ==================== NOTIFICATIONS ====================

    /** FR Customer 7: Get booking notifications for customer */
    @GetMapping("/{customerId}/notifications")
    public ResponseEntity<?> getCustomerNotifications(@PathVariable Long customerId) {
        List<Map<String, Object>> notifications = notificationService.getCustomerNotifications(customerId);
        return ResponseEntity.ok(Map.of("customerId", customerId, "notifications", notifications));
    }

    /** Admin: Get payment failure notifications (from Direct Exchange) */
    @GetMapping("/admin/notifications")
    public ResponseEntity<?> getAdminNotifications() {
        return ResponseEntity.ok(Map.of("notifications", notificationService.getAdminNotifications()));
    }
}
