package com.marketplace.userservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AdminPaymentFailureListener {

    @RabbitListener(
            queues = "${rabbitmq.queue.payment-failed-admin}"
    )
    public void handlePaymentFailure(Object message) {

        System.out.println(
                "[ADMIN PAYMENT FAILURE ALERT] "
                        + message
        );
    }
}