package com.marketplace.userservice.dto;

public class WalletResponse {
    private Long userId;
    private String username;
    private Double balance;
    private String message;

    public WalletResponse() {}

    public WalletResponse(Long userId, String username, Double balance, String message) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
