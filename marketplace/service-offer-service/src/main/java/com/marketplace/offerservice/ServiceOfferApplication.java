package com.marketplace.offerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceOfferApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOfferApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Service Offer Service started on port 8082");
        System.out.println("  H2 Console: http://localhost:8082/h2-console");
        System.out.println("====================================");
    }
}
