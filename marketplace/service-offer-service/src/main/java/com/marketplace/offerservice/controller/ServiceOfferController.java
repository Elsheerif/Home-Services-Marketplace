package com.marketplace.offerservice.controller;

import com.marketplace.offerservice.dto.CreateOfferRequest;

import com.marketplace.offerservice.dto.UpdateOfferRequest;
import com.marketplace.offerservice.entity.ServiceCategory;
import com.marketplace.offerservice.service.ServiceOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/offers")
public class ServiceOfferController {

    @Autowired
    private ServiceOfferService offerService;

    // ==================== CATEGORIES ====================

    /** FR Admin 3: Add new service category */
    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        try {
            ServiceCategory cat = offerService.addCategory(
                    body.get("name"), body.get("description"));
            return ResponseEntity.ok(cat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Get all categories */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(offerService.getAllCategories());
    }

    // ==================== SERVICE OFFERS ====================

    /** FR Provider 6: Create service offer */
    @PostMapping
    public ResponseEntity<?> createOffer(@RequestBody CreateOfferRequest req) {
        try {
            return ResponseEntity.ok(offerService.createOffer(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** FR Provider 7: View all active offers */
    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveOffers() {
        return ResponseEntity.ok(offerService.getAllActiveOffers());
    }

    /** FR Provider 8: Update offer */
    @PutMapping("/{offerId}")
    public ResponseEntity<?> updateOffer(@PathVariable Long offerId,
            @RequestParam Long providerId,
            @RequestBody UpdateOfferRequest req) {
        try {
            return ResponseEntity.ok(offerService.updateOffer(offerId, providerId, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** FR Provider 9: View completed services with customer info */
    @GetMapping("/provider/{providerId}/completed")
    public ResponseEntity<?> getCompletedServices(@PathVariable Long providerId) {
        return ResponseEntity.ok(offerService.getCompletedServices(providerId));
    }

    /** FR Customer 5: Browse available services by category */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<?> getByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(offerService.getOffersByCategory(categoryName));
    }

    /** Get offer by ID */
    @GetMapping("/{offerId}")
    public ResponseEntity<?> getOfferById(@PathVariable Long offerId) {
        try {
            return ResponseEntity.ok(offerService.getOfferById(offerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== INTERNAL APIs ====================

    /** Internal: Mark offer as booked (called by booking service) */
    @PostMapping("/{offerId}/book")
    public ResponseEntity<?> markAsBooked(@PathVariable Long offerId,
            @RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.parseLong(body.get("customerId").toString());
            String customerName = body.get("customerName").toString();
            return ResponseEntity.ok(offerService.markAsBooked(offerId, customerId, customerName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Internal: Revert offer to ACTIVE (called by booking service on rollback) */
    @PostMapping("/{offerId}/revert")
    public ResponseEntity<?> revertOffer(@PathVariable Long offerId) {
        try {
            offerService.revertOfferToActive(offerId);
            return ResponseEntity.ok(Map.of("message", "Offer reverted to ACTIVE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** FR Provider 10: View booking notifications */
    @GetMapping("/provider/{providerId}/notifications")
    public ResponseEntity<?> getProviderNotifications(@PathVariable Long providerId) {
        return ResponseEntity.ok(Map.of(
                "providerId", providerId,
                "notifications", offerService.getProviderNotifications(providerId)));
    }
}
