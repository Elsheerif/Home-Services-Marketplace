package com.marketplace.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Value("${rabbitmq.queue.payment-failed-admin}")
    private String paymentFailedAdminQueue;

    @Value("${rabbitmq.routing.payment-failed}")
    private String paymentFailedRoutingKey;

    // FANOUT EXCHANGE
    @Bean
    public FanoutExchange notificationsExchange() {
        return new FanoutExchange(
                notificationsExchange,
                true,
                false
        );
    }

    // DIRECT EXCHANGE
    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(
                paymentsExchange,
                true,
                false
        );
    }

    // CUSTOMER CONFIRMED QUEUE
    @Bean
    public Queue bookingConfirmedCustomerQueue() {
        return new Queue(
                bookingConfirmedCustomerQueue,
                true
        );
    }

    // CUSTOMER REJECTED QUEUE
    @Bean
    public Queue bookingRejectedCustomerQueue() {
        return new Queue(
                bookingRejectedCustomerQueue,
                true
        );
    }

    // ADMIN PAYMENT FAILED QUEUE
    @Bean
    public Queue paymentFailedAdminQueue() {
        return new Queue(
                paymentFailedAdminQueue,
                true
        );
    }

    // FANOUT BINDING
    @Bean
    public Binding bindingConfirmed() {

        return BindingBuilder
                .bind(bookingConfirmedCustomerQueue())
                .to(notificationsExchange());
    }

    // DIRECT EXCHANGE BINDING
    // ONLY PAYMENT FAILED EVENTS ARRIVE HERE
    @Bean
    public Binding bindingPaymentFailed() {

        return BindingBuilder
                .bind(paymentFailedAdminQueue())
                .to(paymentsExchange())
                .with(paymentFailedRoutingKey);
    }

    @Bean
    @NonNull
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            @NonNull ConnectionFactory connectionFactory
    ) {

        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);

        template.setMessageConverter(
                messageConverter()
        );

        return template;
    }
}