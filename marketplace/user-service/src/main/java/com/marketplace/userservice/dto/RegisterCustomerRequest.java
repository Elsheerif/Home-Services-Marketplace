package com.marketplace.userservice.dto;

public class RegisterCustomerRequest {
    private String username;
    private String password;
    private Double initialBalance;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Double getInitialBalance() { return initialBalance; }
    public void setInitialBalance(Double initialBalance) { this.initialBalance = initialBalance; }
}
