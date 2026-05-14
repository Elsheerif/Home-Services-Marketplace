package com.marketplace.userservice.service;

import com.marketplace.userservice.dto.*;
import com.marketplace.userservice.ejb.SystemStatsBean;
import com.marketplace.userservice.ejb.UserSessionBean;
import com.marketplace.userservice.entity.User;
import com.marketplace.userservice.entity.UserRole;
import com.marketplace.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // EJB: Stateful bean for session management
    @Autowired
    private UserSessionBean userSessionBean;

    // EJB: Singleton bean for system-wide stats
    @Autowired
    private SystemStatsBean systemStatsBean;

    @Transactional
    public UserResponse registerCustomer(RegisterCustomerRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists: " + req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // In production, use BCrypt
        user.setRole(UserRole.CUSTOMER);
        user.setWalletBalance(req.getInitialBalance() != null ? req.getInitialBalance() : 0.0);

        User saved = userRepository.save(user);

        // Singleton EJB: update system-wide stats
        systemStatsBean.incrementRegistration("CUSTOMER");

        return toResponse(saved);
    }

    @Transactional
    public UserResponse registerProvider(RegisterProviderRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists: " + req.getUsername());
        }
        if (req.getProfessionType() == null || req.getProfessionType().isBlank()) {
            throw new RuntimeException("Profession type is required for service providers");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRole(UserRole.PROVIDER);
        user.setProfessionType(req.getProfessionType());

        User saved = userRepository.save(user);

        // Singleton EJB: update system-wide stats
        systemStatsBean.incrementRegistration("PROVIDER");

        return toResponse(saved);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUsername()));

        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Stateful EJB: create a session for this user
        String token = userSessionBean.createSession(user);

        // Singleton EJB: track login count
        systemStatsBean.incrementLogin();

        return new LoginResponse(token, user.getUsername(), user.getRole().name(),
                "Login successful. Active sessions: " + userSessionBean.getActiveSessionCount());
    }

    public Map<String, String> logout(String token) {
        // Stateful EJB: remove session state
        userSessionBean.removeSession(token);
        return Map.of("message", "Logged out successfully");
    }

    @Transactional
    public WalletResponse addFunds(Long userId, AddFundsRequest req) {
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + safeUserId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new RuntimeException("Only customers have wallets");
        }

        double newBalance = user.getWalletBalance() + req.getAmount();
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        return new WalletResponse(safeUserId, user.getUsername(), newBalance,
                "Added $" + req.getAmount() + ". New balance: $" + newBalance);
    }

    public WalletResponse getWalletBalance(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + safeUserId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new RuntimeException("Only customers have wallets");
        }

        return new WalletResponse(safeUserId, user.getUsername(), user.getWalletBalance(), "Current balance");
    }

    // Internal: used by booking service via REST
    public UserResponse getUserById(Long id) {
        Long safeId = Objects.requireNonNull(id, "id must not be null");
        User user = userRepository.findById(safeId)
                .orElseThrow(() -> new RuntimeException("User not found: " + safeId));
        return toResponse(user);
    }

    // Internal: deduct wallet balance (called by booking service)
    @Transactional
    public WalletResponse deductWallet(Long userId, Double amount) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + safeUserId));

        if (user.getWalletBalance() < amount) {
            throw new RuntimeException(
                    "Insufficient balance. Current: $" + user.getWalletBalance() + ", Required: $" + amount);
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        userRepository.save(user);

        return new WalletResponse(safeUserId, user.getUsername(), user.getWalletBalance(),
                "Deducted $" + amount + ". Remaining: $" + user.getWalletBalance());
    }

    // Internal: refund wallet balance (called by booking service on rollback)
    @Transactional
    public WalletResponse refundWallet(Long userId, Double amount) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + safeUserId));

        user.setWalletBalance(user.getWalletBalance() + amount);
        userRepository.save(user);

        return new WalletResponse(safeUserId, user.getUsername(), user.getWalletBalance(),
                "Refunded $" + amount + ". New balance: $" + user.getWalletBalance());
    }

    // Admin: get all users
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Admin: get system stats (from Singleton EJB)
    public Map<String, Object> getSystemStats() {
        return Map.of(
                "totalRegistrations", systemStatsBean.getTotalRegistrations(),
                "totalLogins", systemStatsBean.getTotalLogins(),
                "totalCustomers", systemStatsBean.getTotalCustomerRegistrations(),
                "totalProviders", systemStatsBean.getTotalProviderRegistrations(),
                "activeSessions", userSessionBean.getActiveSessionCount(),
                "systemStartTime", systemStatsBean.getStartTime().toString());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getProfessionType(),
                user.getWalletBalance());
    }
}
