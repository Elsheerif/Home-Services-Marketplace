package com.marketplace.bookingservice.service;

import com.marketplace.bookingservice.dto.BookingRequest;
import com.marketplace.bookingservice.dto.BookingResponse;
import com.marketplace.bookingservice.entity.Booking;
import com.marketplace.bookingservice.entity.BookingStatus;
import com.marketplace.bookingservice.repository.BookingRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${offer-service.url}")
    private String offerServiceUrl;

    @Value("${rabbitmq.exchange.notifications}")
    private String notificationsExchange;

    @Value("${rabbitmq.exchange.payments}")
    private String paymentsExchange;

    @Value("${rabbitmq.queue.booking-confirmed-customer}")
    private String bookingConfirmedCustomerQueue;

    @Value("${rabbitmq.queue.booking-rejected-customer}")
    private String bookingRejectedCustomerQueue;

    @Value("${rabbitmq.routing.payment-failed}")
    private String paymentFailedRoutingKey;

    /**
     * MAIN BOOKING FLOW
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest req) {

        // =========================
        // STEP 1 - FETCH CUSTOMER
        // =========================

        Map<String, Object> customer;

        try {

            ResponseEntity<Map<String, Object>> customerResponse =
                    restTemplate.exchange(
                            userServiceUrl +
                                    "/api/users/" +
                                    req.getCustomerId(),

                            Objects.requireNonNull(HttpMethod.GET),

                            HttpEntity.EMPTY,

                            new ParameterizedTypeReference<Map<String, Object>>() {
                            }
                    );

            customer = customerResponse.getBody();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Customer not found: "
                            + req.getCustomerId()
            );
        }

        if (customer == null ||
                !"CUSTOMER".equals(customer.get("role"))) {

            throw new RuntimeException(
                    "Invalid customer ID: "
                            + req.getCustomerId()
            );
        }

        // =========================
        // STEP 2 - FETCH OFFER
        // =========================

        Map<String, Object> offer;

        try {

            ResponseEntity<Map<String, Object>> offerResponse =
                    restTemplate.exchange(
                            offerServiceUrl +
                                    "/api/offers/" +
                                    req.getOfferId(),

                            Objects.requireNonNull(HttpMethod.GET),

                            HttpEntity.EMPTY,

                            new ParameterizedTypeReference<Map<String, Object>>() {
                            }
                    );

            offer = offerResponse.getBody();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Offer not found: "
                            + req.getOfferId()
            );
        }

        if (offer == null) {

            throw new RuntimeException(
                    "Offer not found: "
                            + req.getOfferId()
            );
        }

        if (!"ACTIVE".equals(offer.get("status"))) {

            throw new RuntimeException(
                    "Offer is not available"
            );
        }

        double price =
                ((Number) offer.get("price")).doubleValue();

        double walletBalance =
                ((Number) customer.get("walletBalance")).doubleValue();

        String customerName =
                customer.get("username").toString();

        String providerName =
                offer.get("providerName").toString();

        // =========================
        // STEP 3 - WALLET CHECK
        // =========================

        if (walletBalance < price) {

            String reason =
                    "Insufficient wallet balance";

            Booking rejected =
                    buildBooking(
                            req,
                            customer,
                            offer,
                            price
                    );

            rejected.setStatus(BookingStatus.REJECTED);
            rejected.setRejectionReason(reason);
            rejected.setUpdatedAt(LocalDateTime.now());

            bookingRepository.save(rejected);

            Map<String, Object> rejectMsg =
                    buildNotification(
                            rejected,
                            "BOOKING_REJECTED",
                            reason
                    );

            rabbitTemplate.convertAndSend(
                    bookingRejectedCustomerQueue,
                    rejectMsg
            );

            // DIRECT EXCHANGE EVENT
            Map<String, Object> paymentFailedMsg =
                    new HashMap<>(rejectMsg);

            paymentFailedMsg.put(
                    "eventType",
                    "PaymentFailed"
            );

            rabbitTemplate.convertAndSend(
                    paymentsExchange,
                    paymentFailedRoutingKey,
                    paymentFailedMsg
            );

            return toResponse(rejected);
        }

        // =========================
        // STEP 4 - DEDUCT WALLET
        // =========================

        Map<String, Double> deductBody =
                Map.of("amount", price);

        try {

            restTemplate.postForObject(
                    userServiceUrl +
                            "/api/users/" +
                            req.getCustomerId() +
                            "/wallet/deduct",

                    deductBody,

                    Map.class
            );

        } catch (Exception e) {

            String reason =
                    "Payment deduction failed";

            handleBookingFailure(
                    req,
                    customer,
                    offer,
                    price,
                    reason
            );

            throw new RuntimeException(reason);
        }

        // =========================
        // STEP 5 - BOOK OFFER
        // =========================

        Map<String, Object> bookBody =
                new HashMap<>();

        bookBody.put(
                "customerId",
                req.getCustomerId()
        );

        bookBody.put(
                "customerName",
                customerName
        );

        try {

            restTemplate.postForObject(
                    offerServiceUrl +
                            "/api/offers/" +
                            req.getOfferId() +
                            "/book",

                    bookBody,

                    Map.class
            );

        } catch (Exception e) {

            // ROLLBACK REFUND

            Map<String, Double> refundBody =
                    Map.of("amount", price);

            restTemplate.postForObject(
                    userServiceUrl +
                            "/api/users/" +
                            req.getCustomerId() +
                            "/wallet/refund",

                    refundBody,

                    Map.class
            );

            String reason =
                    "Offer booking failed";

            handleBookingFailure(
                    req,
                    customer,
                    offer,
                    price,
                    reason
            );

            throw new RuntimeException(reason);
        }

        // =========================
        // STEP 6 - SAVE BOOKING
        // =========================

        Booking booking =
                buildBooking(
                        req,
                        customer,
                        offer,
                        price
                );

        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        booking.setUpdatedAt(
                LocalDateTime.now()
        );

        bookingRepository.save(booking);

        // =========================
        // STEP 7 - SEND NOTIFICATIONS
        // =========================

        Map<String, Object> confirmMsg =
                buildNotification(
                        booking,
                        "BOOKING_CONFIRMED",
                        null
                );

        rabbitTemplate.convertAndSend(
                notificationsExchange,
                "",
                confirmMsg
        );

        System.out.println(
                "[BOOKING CONFIRMED] #" +
                        booking.getId()
        );

        return toResponse(booking);
    }

    private void handleBookingFailure(
            BookingRequest req,
            Map<String, Object> customer,
            Map<String, Object> offer,
            double price,
            String reason
    ) {

        Booking rejected =
                buildBooking(
                        req,
                        customer,
                        offer,
                        price
                );

        rejected.setStatus(
                BookingStatus.REJECTED
        );

        rejected.setRejectionReason(reason);

        rejected.setUpdatedAt(
                LocalDateTime.now()
        );

        bookingRepository.save(rejected);

        Map<String, Object> rejectMsg =
                buildNotification(
                        rejected,
                        "BOOKING_REJECTED",
                        reason
                );

        rabbitTemplate.convertAndSend(
                bookingRejectedCustomerQueue,
                rejectMsg
        );

        // DIRECT EXCHANGE
        Map<String, Object> paymentFailedMsg =
                new HashMap<>(rejectMsg);

        paymentFailedMsg.put(
                "eventType",
                "PaymentFailed"
        );

        rabbitTemplate.convertAndSend(
                paymentsExchange,
                paymentFailedRoutingKey,
                paymentFailedMsg
        );
    }

    private Booking buildBooking(
            BookingRequest req,
            Map<String, Object> customer,
            Map<String, Object> offer,
            double price
    ) {

        Booking b = new Booking();

        b.setCustomerId(req.getCustomerId());

        b.setCustomerName(
                customer.get("username").toString()
        );

        b.setOfferId(req.getOfferId());

        b.setProviderId(
                Long.parseLong(
                        offer.get("providerId").toString()
                )
        );

        b.setProviderName(
                offer.get("providerName").toString()
        );

        b.setServiceDescription(
                offer.get("description").toString()
        );

        b.setAmount(price);

        b.setCategoryName(
                offer.get("categoryName") != null
                        ? offer.get("categoryName").toString()
                        : "N/A"
        );

        if (offer.get("availableDate") != null) {

            b.setServiceDate(
                    LocalDate.parse(
                            offer.get("availableDate").toString()
                    )
            );
        }

        return b;
    }

    private Map<String, Object> buildNotification(
            Booking booking,
            String type,
            String reason
    ) {

        Map<String, Object> msg =
                new HashMap<>();

        msg.put("bookingId", booking.getId());
        msg.put("customerId", booking.getCustomerId());
        msg.put("customerName", booking.getCustomerName());
        msg.put("providerId", booking.getProviderId());
        msg.put("providerName", booking.getProviderName());
        msg.put("serviceDescription", booking.getServiceDescription());
        msg.put("amount", booking.getAmount());
        msg.put("status", booking.getStatus().name());
        msg.put("notificationType", type);
        msg.put("timestamp", LocalDateTime.now().toString());

        if (reason != null) {
            msg.put("reason", reason);
        }

        return msg;
    }

    public List<BookingResponse> getCustomerBookings(
            Long customerId
    ) {

        return bookingRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {

        return bookingRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {

        Booking b =
                bookingRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking not found"
                                )
                        );

        return toResponse(b);
    }

    private BookingResponse toResponse(Booking b) {

        BookingResponse r =
                new BookingResponse();

        r.setId(b.getId());
        r.setCustomerId(b.getCustomerId());
        r.setCustomerName(b.getCustomerName());
        r.setOfferId(b.getOfferId());
        r.setProviderId(b.getProviderId());
        r.setProviderName(b.getProviderName());
        r.setServiceDescription(b.getServiceDescription());
        r.setAmount(b.getAmount());
        r.setCategoryName(b.getCategoryName());
        r.setServiceDate(b.getServiceDate());
        r.setStatus(b.getStatus().name());
        r.setRejectionReason(b.getRejectionReason());
        r.setCreatedAt(b.getCreatedAt());

        return r;
    }
}