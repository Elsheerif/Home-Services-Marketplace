package com.marketplace.userservice.dto;

public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private String professionType;
    private Double walletBalance;

    public UserResponse() {}

    public UserResponse(Long id, String username, String role, String professionType, Double walletBalance) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.professionType = professionType;
        this.walletBalance = walletBalance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getProfessionType() { return professionType; }
    public void setProfessionType(String professionType) { this.professionType = professionType; }
    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }
}
