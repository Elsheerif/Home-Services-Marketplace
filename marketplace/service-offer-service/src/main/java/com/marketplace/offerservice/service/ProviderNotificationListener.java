package com.marketplace.offerservice.service;

import com.marketplace.offerservice.dto.ProviderRequestNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProviderNotificationListener {

    @Autowired
    private ServiceOfferService offerService;

    /**
     * Existing booking confirmation listener
     */
    @RabbitListener(queues = "${rabbitmq.queue.booking-confirmed-provider}")
    public void handleBookingConfirmed(Map<String, Object> message) {

        System.out.println(
                "[RabbitMQ] Provider received booking confirmation: "
                        + message
        );

        message.put("notificationType", "BOOKING_CONFIRMED");

        offerService.addProviderNotification(message);
    }

    /**
     * NEW:
     * Provider receives matching customer service requests
     */
    @RabbitListener(queues = "${rabbitmq.queue.service-request-provider}")
    public void handleServiceRequestNotification(
            ProviderRequestNotification notification
    ) {

        System.out.println(
                "[RabbitMQ] Provider received service request: "
                        + notification.getRequestId()
        );
    }
}