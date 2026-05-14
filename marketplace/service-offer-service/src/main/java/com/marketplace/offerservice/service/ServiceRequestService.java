package com.marketplace.offerservice.service;

import com.marketplace.offerservice.dto.CreateServiceRequestDto;
import com.marketplace.offerservice.dto.ProviderRequestNotification;
import com.marketplace.offerservice.entity.OfferStatus;
import com.marketplace.offerservice.entity.RequestStatus;
import com.marketplace.offerservice.entity.ServiceOffer;
import com.marketplace.offerservice.entity.ServiceRequest;
import com.marketplace.offerservice.repository.ServiceOfferRepository;
import com.marketplace.offerservice.repository.ServiceRequestRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceRequestService {

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private ServiceOfferRepository offerRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${offer-service.url}")
    private String offerServiceUrl;

    @Value("${rabbitmq.exchange.notifications}")
    private String notificationsExchange;

    public Object createRequest(CreateServiceRequestDto dto) {

        ServiceRequest request = new ServiceRequest();

        request.setCustomerId(dto.getCustomerId());
        request.setCategory(dto.getCategory());
        request.setMaxPrice(dto.getMaxPrice());
        request.setRequiredDate(dto.getRequiredDate());
        request.setStatus(RequestStatus.PENDING);

        requestRepository.save(request);

        List<ServiceOffer> matchedOffers =
                offerRepository
                        .findByCategoryNameIgnoreCaseAndPriceLessThanEqualAndAvailableDateAndStatus(
                                dto.getCategory(),
                                dto.getMaxPrice(),
                                dto.getRequiredDate(),
                                OfferStatus.ACTIVE
                        );

        for (ServiceOffer offer : matchedOffers) {

            ProviderRequestNotification notification =
                    new ProviderRequestNotification();

            notification.setRequestId(request.getId());
            notification.setProviderId(offer.getProviderId());
            notification.setCategory(dto.getCategory());
            notification.setMaxPrice(dto.getMaxPrice());
            notification.setRequiredDate(dto.getRequiredDate());

            rabbitTemplate.convertAndSend(
                    notificationsExchange,
                    "",
                    notification
            );
        }

        Map<String, Object> response = new HashMap<>();

        response.put("requestId", request.getId());
        response.put("matchedOffers", matchedOffers.size());

        return response;
    }

    public Object acceptRequest(
            Long requestId,
            Long providerId
    ) {

        ServiceRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new RuntimeException("Request not found"));

        List<ServiceOffer> offers =
                offerRepository.findByProviderIdAndStatus(
                        providerId,
                        OfferStatus.ACTIVE
                );

        if (offers.isEmpty()) {
            throw new RuntimeException(
                    "No active offer for provider"
            );
        }

        ServiceOffer selectedOffer = offers.get(0);

        Map<String, Object> bookingRequest =
                new HashMap<>();

        bookingRequest.put(
                "customerId",
                request.getCustomerId()
        );

        bookingRequest.put(
                "offerId",
                selectedOffer.getId()
        );

        Object bookingResponse =
                restTemplate.postForObject(
                        "http://localhost:8083/api/bookings",
                        new HttpEntity<>(bookingRequest),
                        Object.class
                );

        request.setStatus(RequestStatus.ACCEPTED);

        requestRepository.save(request);

        return bookingResponse;
    }
}