package com.homework.backend.controllers;

import com.homework.backend.dto.EnrollmentResponse;
import com.homework.backend.dto.PaymentConfirmationRequest;
import com.homework.backend.dto.PaymentIntentRequest;
import com.homework.backend.dto.PaymentIntentResponse;
import com.homework.backend.dto.PaymentRefundRequest;
import com.homework.backend.models.User;
import com.homework.backend.services.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/intent")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request,
                                                                     @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request, student));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentResponse> confirmPayment(@Valid @RequestBody PaymentConfirmationRequest request,
                                                             @AuthenticationPrincipal User student) {
        EnrollmentResponse enrollment = paymentService.confirmPayment(request.paymentIntentId(), student);
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Void> refund(@Valid @RequestBody PaymentRefundRequest request,
                                       @AuthenticationPrincipal User student) {
        paymentService.refundEnrollment(request, student);
        return ResponseEntity.noContent().build();
    }
}

