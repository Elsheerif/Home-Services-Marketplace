package com.marketplace.offerservice.controller;

import com.marketplace.offerservice.dto.CreateServiceRequestDto;
import com.marketplace.offerservice.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody CreateServiceRequestDto dto
    ) {

        return ResponseEntity.ok(
                serviceRequestService.createRequest(dto)
        );
    }

    @PostMapping("/{requestId}/accept/{providerId}")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long requestId,
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                serviceRequestService.acceptRequest(
                        requestId,
                        providerId
                )
        );
    }
}