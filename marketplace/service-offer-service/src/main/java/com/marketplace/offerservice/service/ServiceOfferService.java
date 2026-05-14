package com.marketplace.offerservice.service;

import com.marketplace.offerservice.dto.CreateOfferRequest;
import com.marketplace.offerservice.dto.OfferResponse;
import com.marketplace.offerservice.dto.UpdateOfferRequest;
import com.marketplace.offerservice.entity.OfferStatus;
import com.marketplace.offerservice.entity.ServiceCategory;
import com.marketplace.offerservice.entity.ServiceOffer;
import com.marketplace.offerservice.repository.CategoryRepository;
import com.marketplace.offerservice.repository.ServiceOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceOfferService {

    @Autowired
    private ServiceOfferRepository offerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    // In-memory provider notifications (received from RabbitMQ)
    private final List<Map<String, Object>> providerNotifications = new ArrayList<>();

    // ==================== CATEGORIES ====================

    /** FR Admin 3: Add new service category */
    @Transactional
    public ServiceCategory addCategory(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Category already exists: " + name);
        }
        return categoryRepository.save(new ServiceCategory(name, description));
    }

    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ==================== SERVICE OFFERS ====================

    /** FR Provider 6: Create service offer */
    @Transactional
    public OfferResponse createOffer(CreateOfferRequest req) {
        // Validate provider exists via REST call to user-service
        @SuppressWarnings("unchecked")
        Map<String, Object> provider = (Map<String, Object>) restTemplate.getForObject(
                userServiceUrl + "/api/users/" + req.getProviderId(), Map.class);
        try {
        } catch (Exception e) {
            throw new RuntimeException("Provider not found with id: " + req.getProviderId());
        }

        if (provider == null)
            throw new RuntimeException("Provider not found");
        if (!"PROVIDER".equals(provider.get("role"))) {
            throw new RuntimeException("User is not a service provider");
        }

        ServiceCategory category = categoryRepository.findByNameIgnoreCase(req.getCategoryName())
                .orElseThrow(() -> new RuntimeException("Category not found: " + req.getCategoryName()));

        ServiceOffer offer = new ServiceOffer();
        offer.setProviderId(req.getProviderId());
        offer.setProviderName(provider.get("username").toString());
        offer.setProfessionType(
                provider.get("professionType") != null ? provider.get("professionType").toString() : "General");
        offer.setDescription(req.getDescription());
        offer.setPrice(req.getPrice());
        offer.setAvailableDate(req.getAvailableDate());
        offer.setCategory(category);

        return toResponse(offerRepository.save(offer));
    }

    /** FR Provider 7: View all active service offers */
    public List<OfferResponse> getAllActiveOffers() {
        return offerRepository.findByStatus(OfferStatus.ACTIVE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** FR Provider 8: Update offer pricing and availability */
    @Transactional
    public OfferResponse updateOffer(Long offerId, Long providerId, UpdateOfferRequest req) {
        ServiceOffer offer = offerRepository.findById(Objects.requireNonNull(offerId, "offerId must not be null"))
                .orElseThrow(() -> new RuntimeException("Offer not found: " + offerId));

        if (!offer.getProviderId().equals(providerId)) {
            throw new RuntimeException("You can only update your own offers");
        }

        if (offer.getStatus() == OfferStatus.BOOKED) {
            throw new RuntimeException("Cannot update a booked offer");
        }

        if (req.getPrice() != null)
            offer.setPrice(req.getPrice());
        if (req.getAvailableDate() != null)
            offer.setAvailableDate(req.getAvailableDate());
        if (req.getDescription() != null)
            offer.setDescription(req.getDescription());

        return toResponse(offerRepository.save(offer));
    }

    /** FR Provider 9: View completed services with customer info */
    public List<OfferResponse> getCompletedServices(Long providerId) {
        return offerRepository.findByProviderIdAndStatus(providerId, OfferStatus.BOOKED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** FR Customer 5: Browse available services by category */
    public List<OfferResponse> getOffersByCategory(String categoryName) {
        return offerRepository.findByCategoryNameIgnoreCaseAndStatus(categoryName, OfferStatus.ACTIVE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Get offer by ID (used internally by booking service) */
    public OfferResponse getOfferById(Long offerId) {
        ServiceOffer offer = offerRepository.findById(Objects.requireNonNull(offerId, "offerId must not be null"))
                .orElseThrow(() -> new RuntimeException("Offer not found: " + offerId));
        return toResponse(offer);
    }

    /** Internal: Mark offer as booked (called by booking service) */
    @Transactional
    public OfferResponse markAsBooked(Long offerId, Long customerId, String customerName) {
        ServiceOffer offer = offerRepository.findById(Objects.requireNonNull(offerId, "offerId must not be null"))
                .orElseThrow(() -> new RuntimeException("Offer not found: " + offerId));

        if (offer.getStatus() != OfferStatus.ACTIVE) {
            throw new RuntimeException("Offer is not available for booking");
        }

        offer.setStatus(OfferStatus.BOOKED);
        offer.setBookedByCustomerId(customerId);
        offer.setBookedByCustomerName(customerName);
        offer.setBookedAt(LocalDateTime.now());

        return toResponse(offerRepository.save(offer));
    }

    /** Internal: Revert offer to ACTIVE (rollback on booking failure) */
    @Transactional
    public void revertOfferToActive(Long offerId) {
        ServiceOffer offer = offerRepository.findById(Objects.requireNonNull(offerId, "offerId must not be null"))
                .orElse(null);
        if (offer != null) {
            offer.setStatus(OfferStatus.ACTIVE);
            offer.setBookedByCustomerId(null);
            offer.setBookedByCustomerName(null);
            offer.setBookedAt(null);
            offerRepository.save(offer);
        }
    }

    /** FR Provider 10: Receive and view booking info */
    public void addProviderNotification(Map<String, Object> notification) {
        providerNotifications.add(notification);
    }

    public List<Map<String, Object>> getProviderNotifications(Long providerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> n : providerNotifications) {
            Object pId = n.get("providerId");
            if (pId != null && pId.toString().equals(providerId.toString())) {
                result.add(n);
            }
        }
        return result;
    }

    private OfferResponse toResponse(ServiceOffer offer) {
        OfferResponse r = new OfferResponse();
        r.setId(offer.getId());
        r.setProviderId(offer.getProviderId());
        r.setProviderName(offer.getProviderName());
        r.setProfessionType(offer.getProfessionType());
        r.setDescription(offer.getDescription());
        r.setPrice(offer.getPrice());
        r.setAvailableDate(offer.getAvailableDate());
        r.setStatus(offer.getStatus().name());
        r.setCategoryName(offer.getCategory() != null ? offer.getCategory().getName() : null);
        r.setBookedByCustomerId(offer.getBookedByCustomerId());
        r.setBookedByCustomerName(offer.getBookedByCustomerName());
        r.setBookedAt(offer.getBookedAt());
        return r;
    }
}
