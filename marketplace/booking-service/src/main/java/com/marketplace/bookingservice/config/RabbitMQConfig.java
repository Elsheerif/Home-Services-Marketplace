package com.marketplace.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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

    @Bean
    public FanoutExchange notificationsExchange() {
        return new FanoutExchange(notificationsExchange, true, false);
    }

    // DIRECT EXCHANGE FOR PAYMENT FAILURES
    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(paymentsExchange, true, false);
    }

    @Bean
    public Queue bookingConfirmedCustomerQueue() {
        return new Queue(bookingConfirmedCustomerQueue, true);
    }

    @Bean
    public Queue bookingRejectedCustomerQueue() {
        return new Queue(bookingRejectedCustomerQueue, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        if (connectionFactory == null) {
            throw new IllegalArgumentException(
                    "ConnectionFactory cannot be null"
            );
        }

        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);

        MessageConverter converter = messageConverter();

        if (converter != null) {
            template.setMessageConverter(converter);
        }

        return template;
    }
}