package com.marketplace.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Booking Service started on port 8083");
        System.out.println("  H2 Console: http://localhost:8083/h2-console");
        System.out.println("  RabbitMQ: Fanout + Direct Exchange for notifications");
        System.out.println("====================================");
    }
}
