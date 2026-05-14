package com.marketplace.offerservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OfferResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private String professionType;
    private String description;
    private Double price;
    private LocalDate availableDate;
    private String status;
    private String categoryName;
    private Long bookedByCustomerId;
    private String bookedByCustomerName;
    private LocalDateTime bookedAt;

    public OfferResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProfessionType() { return professionType; }
    public void setProfessionType(String professionType) { this.professionType = professionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getBookedByCustomerId() { return bookedByCustomerId; }
    public void setBookedByCustomerId(Long bookedByCustomerId) { this.bookedByCustomerId = bookedByCustomerId; }
    public String getBookedByCustomerName() { return bookedByCustomerName; }
    public void setBookedByCustomerName(String bookedByCustomerName) { this.bookedByCustomerName = bookedByCustomerName; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}
