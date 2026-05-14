package com.marketplace.offerservice.dto;

import java.time.LocalDate;

public class UpdateOfferRequest {
    private Double price;
    private LocalDate availableDate;
    private String description;

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
