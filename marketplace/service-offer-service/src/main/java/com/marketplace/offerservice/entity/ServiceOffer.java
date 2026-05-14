package com.marketplace.offerservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_offers")
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String professionType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Enumerated(EnumType.STRING)
    private OfferStatus status; // ACTIVE, BOOKED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ServiceCategory category;

    private LocalDateTime createdAt;

    // When booked, store customer info
    private Long bookedByCustomerId;
    private String bookedByCustomerName;
    private LocalDateTime bookedAt;

    public ServiceOffer() {
        this.status = OfferStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
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
    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getBookedByCustomerId() { return bookedByCustomerId; }
    public void setBookedByCustomerId(Long bookedByCustomerId) { this.bookedByCustomerId = bookedByCustomerId; }
    public String getBookedByCustomerName() { return bookedByCustomerName; }
    public void setBookedByCustomerName(String bookedByCustomerName) { this.bookedByCustomerName = bookedByCustomerName; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}
