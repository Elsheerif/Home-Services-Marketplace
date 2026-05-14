package com.marketplace.offerservice.config;

import com.marketplace.offerservice.entity.ServiceCategory;
import com.marketplace.offerservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Autowired
    private CategoryRepository categoryRepository;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner initCategories() {
        return args -> {
            String[][] categories = {
                    {"Plumbing", "Plumbing repairs and installations"},
                    {"Carpentry", "Carpentry and woodwork services"},
                    {"Electrical", "Electrical repairs and wiring"},
                    {"Cleaning", "Home and office cleaning services"},
                    {"Painting", "Interior and exterior painting"}
            };
            for (String[] cat : categories) {
                if (!categoryRepository.existsByNameIgnoreCase(cat[0])) {
                    categoryRepository.save(new ServiceCategory(cat[0], cat[1]));
                    System.out.println("[DataInitializer] Category created: " + cat[0]);
                }
            }
        };
    }
}
