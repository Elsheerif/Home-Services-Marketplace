package com.marketplace.offerservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.notifications}")
    private String notificationsExchange;

    @Value("${rabbitmq.queue.booking-confirmed-provider}")
    private String bookingConfirmedProviderQueue;

    // NEW QUEUE FOR SERVICE REQUESTS
    @Value("${rabbitmq.queue.service-request-provider}")
    private String serviceRequestProviderQueue;

    @Bean
    public FanoutExchange notificationsExchange() {
        return new FanoutExchange(notificationsExchange, true, false);
    }

    @Bean
    public Queue bookingConfirmedProviderQueue() {
        return new Queue(bookingConfirmedProviderQueue, true);
    }

    // NEW QUEUE
    @Bean
    public Queue serviceRequestProviderQueue() {
        return new Queue(serviceRequestProviderQueue, true);
    }

    @Bean
    public Binding bindingProviderConfirmed() {
        return BindingBuilder.bind(bookingConfirmedProviderQueue())
                .to(notificationsExchange());
    }

    // NEW BINDING
    @Bean
    public Binding bindingServiceRequestProvider() {
        return BindingBuilder.bind(serviceRequestProviderQueue())
                .to(notificationsExchange());
    }

    @Bean
    @NonNull
    public org.springframework.amqp.support.converter.MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(@NonNull ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}