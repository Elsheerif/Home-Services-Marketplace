package com.marketplace.bookingservice.controller;

import com.marketplace.bookingservice.dto.BookingRequest;
import com.marketplace.bookingservice.dto.BookingResponse;
import com.marketplace.bookingservice.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Existing booking endpoint
     */
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest req
    ) {

        try {

            BookingResponse response =
                    bookingService.createBooking(req);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * NEW:
     * Used by provider accepted request flow
     */
    @PostMapping("/request-booking")
    public ResponseEntity<?> createBookingFromRequest(
            @RequestBody BookingRequest req
    ) {

        try {

            BookingResponse response =
                    bookingService.createBooking(req);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * FR Customer 7b:
     * View booking history
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerBookings(
            @PathVariable Long customerId
    ) {

        return ResponseEntity.ok(
                bookingService.getCustomerBookings(customerId)
        );
    }

    /**
     * FR Admin 2:
     * View all transaction history
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllBookings() {

        return ResponseEntity.ok(
                bookingService.getAllBookings()
        );
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long bookingId
    ) {

        try {

            return ResponseEntity.ok(
                    bookingService.getBookingById(bookingId)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}