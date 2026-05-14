package com.marketplace.offerservice.repository;

import com.marketplace.offerservice.entity.OfferStatus;
import com.marketplace.offerservice.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {

    List<ServiceOffer> findByProviderId(Long providerId);

    List<ServiceOffer> findByStatus(OfferStatus status);

    List<ServiceOffer> findByCategoryNameIgnoreCaseAndStatus(
            String categoryName,
            OfferStatus status
    );

    List<ServiceOffer> findByProviderIdAndStatus(
            Long providerId,
            OfferStatus status
    );

    // NEW MATCHING QUERY
    List<ServiceOffer> findByCategoryNameIgnoreCaseAndPriceLessThanEqualAndAvailableDateAndStatus(
            String categoryName,
            Double price,
            LocalDate availableDate,
            OfferStatus status
    );
}