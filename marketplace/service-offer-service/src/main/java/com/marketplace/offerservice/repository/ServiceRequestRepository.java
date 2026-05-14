package com.marketplace.offerservice.repository;

import com.marketplace.offerservice.entity.RequestStatus;
import com.marketplace.offerservice.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    // Get all requests created by a customer
    List<ServiceRequest> findByCustomerId(Long customerId);

    // Get requests by status (PENDING, ACCEPTED, COMPLETED)
    List<ServiceRequest> findByStatus(RequestStatus status);

    // Find matching requests by category and date
    List<ServiceRequest> findByCategoryIgnoreCaseAndRequiredDate(
            String category,
            LocalDate requiredDate
    );

    // Find matching requests by category/date/status
    List<ServiceRequest> findByCategoryIgnoreCaseAndRequiredDateAndStatus(
            String category,
            LocalDate requiredDate,
            RequestStatus status
    );
}