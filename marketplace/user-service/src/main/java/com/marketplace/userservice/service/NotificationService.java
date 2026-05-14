package com.marketplace.userservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    // In-memory store of notifications (in production, use a DB)
    private final List<Map<String, Object>> customerNotifications = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> adminNotifications = new CopyOnWriteArrayList<>();

    /**
     * Listens to booking confirmations from RabbitMQ (Fanout Exchange)
     */
    @RabbitListener(queues = "${rabbitmq.queue.booking-confirmed-customer}")
    public void handleBookingConfirmed(Map<String, Object> message) {
        System.out.println("[RabbitMQ] Received booking confirmation: " + message);
        message.put("notificationType", "BOOKING_CONFIRMED");
        customerNotifications.add(message);
    }

    /**
     * Listens to booking rejections from RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.booking-rejected-customer}")
    public void handleBookingRejected(Map<String, Object> message) {
        System.out.println("[RabbitMQ] Received booking rejection: " + message);
        message.put("notificationType", "BOOKING_REJECTED");
        customerNotifications.add(message);
    }

    /**
     * Listens to payment failure events from RabbitMQ Direct Exchange (routing key: PaymentFailed)
     * Admin-only notifications
     */
    @RabbitListener(queues = "${rabbitmq.queue.payment-failed-admin}")
    public void handlePaymentFailed(Map<String, Object> message) {
        System.out.println("[RabbitMQ Direct Exchange] PaymentFailed event received by admin: " + message);
        message.put("notificationType", "PAYMENT_FAILED");
        adminNotifications.add(message);
    }

    public List<Map<String, Object>> getCustomerNotifications(Long customerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> n : customerNotifications) {
            Object cId = n.get("customerId");
            if (cId != null && cId.toString().equals(customerId.toString())) {
                result.add(n);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getAdminNotifications() {
        return new ArrayList<>(adminNotifications);
    }

    public List<Map<String, Object>> getAllCustomerNotifications() {
        return new ArrayList<>(customerNotifications);
    }
}
