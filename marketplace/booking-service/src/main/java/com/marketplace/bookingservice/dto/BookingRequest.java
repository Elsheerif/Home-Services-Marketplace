package com.marketplace.bookingservice.dto;

public class BookingRequest {
    private Long customerId;
    private Long offerId;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }
}
