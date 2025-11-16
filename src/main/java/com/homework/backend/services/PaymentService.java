package com.homework.backend.services;

import com.homework.backend.config.props.StripeProperties;
import com.homework.backend.dto.EnrollmentResponse;
import com.homework.backend.dto.PaymentIntentRequest;
import com.homework.backend.dto.PaymentIntentResponse;
import com.homework.backend.dto.PaymentRefundRequest;
import com.homework.backend.models.*;
import com.homework.backend.repositories.CourseRepository;
import com.homework.backend.repositories.EnrollmentRepository;
import com.homework.backend.repositories.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal PLATFORM_FEE_PERCENT = new BigDecimal("0.10");

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final EnrollmentService enrollmentService;
    private final StripeProperties stripeProperties;
    private final EmailService emailService;

    public PaymentService(CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          PaymentRepository paymentRepository,
                          EnrollmentService enrollmentService,
                          StripeProperties stripeProperties,
                          EmailService emailService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentRepository = paymentRepository;
        this.enrollmentService = enrollmentService;
        this.stripeProperties = stripeProperties;
        this.emailService = emailService;
    }

    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request, User student) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Cours non trouvé."));

        if (course.getTeacher().getId().equals(student.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas vous inscrire à votre propre cours.");
        }

        boolean hasActiveEnrollment = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                student.getId(),
                course.getId(),
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED)
        );
        if (hasActiveEnrollment) {
            throw new IllegalStateException("Vous êtes déjà inscrit à ce cours.");
        }

        // Prevent duplicate pending payments
        boolean pendingPaymentExists = paymentRepository.existsByCourseIdAndStudentIdAndStatusIn(
                course.getId(),
                student.getId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.REQUIRES_ACTION)
        );
        if (pendingPaymentExists) {
            throw new IllegalStateException("Un paiement est déjà en cours pour ce cours. Veuillez finaliser le paiement existant.");
        }

        BigDecimal amount = course.getPrice();
        validateAmount(amount);

        BigDecimal platformFee = amount.multiply(PLATFORM_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal teacherAmount = amount.subtract(platformFee);

        Payment payment = Payment.builder()
                .student(student)
                .course(course)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .platformFee(platformFee)
                .teacherAmount(teacherAmount)
                .currency("eur")
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(convertToStripeAmount(amount))
                    .setCurrency(payment.getCurrency())
                    .setReceiptEmail(student.getEmail())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                    .builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putAllMetadata(Map.of(
                            "paymentId", payment.getId().toString(),
                            "courseId", course.getId().toString(),
                            "studentId", student.getId().toString()
                    ))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            payment.setStripePaymentIntentId(intent.getId());
            paymentRepository.save(payment);

            return new PaymentIntentResponse(
                    intent.getClientSecret(),
                    intent.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getPlatformFee(),
                    payment.getTeacherAmount(),
                    payment.getId(),
                    stripeProperties.getPublishableKey()
            );
        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent: {}", e.getMessage(), e);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalStateException("Impossible de créer le paiement. Veuillez réessayer plus tard.");
        }
    }

    public EnrollmentResponse confirmPayment(String paymentIntentId, User student) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable."));

        if (!payment.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Vous ne pouvez pas confirmer ce paiement.");
        }

        if (payment.getStatus() == PaymentStatus.SUCCEEDED && payment.getEnrollment() != null) {
            return enrollmentService.toResponse(payment.getEnrollment());
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(
                    paymentIntentId,
                    PaymentIntentRetrieveParams.builder().build(),
                    null
            );

            switch (intent.getStatus()) {
                case "requires_action" -> {
                    payment.setStatus(PaymentStatus.REQUIRES_ACTION);
                    paymentRepository.save(payment);
                    throw new IllegalStateException("Le paiement nécessite une action supplémentaire.");
                }
                case "succeeded" -> {
                    Enrollment enrollment = enrollmentService.createEnrollment(payment.getCourse().getId(), student);
                    payment.setEnrollment(enrollment);
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    paymentRepository.save(payment);
                    emailService.sendEnrollmentConfirmationEmail(student, payment.getCourse());
                    return enrollmentService.toResponse(enrollment);
                }
                default -> throw new IllegalStateException("Statut de paiement inattendu : " + intent.getStatus());
            }
        } catch (StripeException e) {
            log.error("Erreur Stripe lors de la confirmation du paiement: {}", e.getMessage(), e);
            throw new IllegalStateException("Impossible de confirmer le paiement. Veuillez contacter le support.");
        }
    }

    public void refundEnrollment(PaymentRefundRequest request, User student) {
        Payment payment = paymentRepository.findByEnrollmentId(request.enrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Aucun paiement trouvé pour cette inscription."));

        if (!payment.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Vous ne pouvez pas annuler cette inscription.");
        }

        Enrollment enrollment = payment.getEnrollment();
        if (enrollment == null) {
            throw new IllegalStateException("Aucune inscription associée à ce paiement.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("Cette inscription a déjà été annulée.");
        }

        Course course = payment.getCourse();
        validateRefundWindow(course.getCourseDateTime());

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .build();
            Refund refund = Refund.create(params);

            payment.setStripeRefundId(refund.getId());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            enrollment.setStatus(EnrollmentStatus.CANCELLED);
            enrollmentRepository.save(enrollment);

            emailService.sendEnrollmentCancellationEmail(student, course);
        } catch (StripeException e) {
            log.error("Erreur Stripe lors du remboursement: {}", e.getMessage(), e);
            throw new IllegalStateException("Impossible de rembourser le paiement. Veuillez contacter le support.");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du cours est invalide.");
        }
    }

    private void validateRefundWindow(LocalDateTime courseDateTime) {
        if (courseDateTime == null) {
            throw new IllegalStateException("La date du cours est manquante. Impossible de traiter le remboursement.");
        }

        Duration untilCourse = Duration.between(LocalDateTime.now(), courseDateTime);
        if (untilCourse.toHours() < 48) {
            throw new IllegalStateException("Impossible d'annuler moins de 48 heures avant le cours.");
        }
    }

    private long convertToStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}

