package com.homework.backend.services;

import com.homework.backend.config.props.FrontendProperties;
import com.homework.backend.models.Course;
import com.homework.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private static final DateTimeFormatter COURSE_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRENCH);

	private final JavaMailSender mailSender;
	private final FrontendProperties frontendProperties;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public EmailService(JavaMailSender mailSender, FrontendProperties frontendProperties) {
		this.mailSender = mailSender;
		this.frontendProperties = frontendProperties;
	}

	public void sendVerificationEmail(String to, String token, String firstName) {
		log.info("Attempting to send verification email to: {}", to);
		log.debug("SMTP configured - From: {}, Host: smtp.gmail.com, Port: 587", fromEmail);

		String verificationUrl = frontendProperties.getUrl() + "/verify-email?token=" + token;
		log.debug("Verification URL: {}", verificationUrl);

		String body = String.format(
				"Bonjour %s,%n%n" +
						"Merci de vous être inscrit sur HomeWork !%n%n" +
						"Pour activer votre compte, veuillez cliquer sur le lien suivant :%n" +
						"%s%n%n" +
						"Ce lien est valide pendant 24 heures.%n%n" +
						"Si vous n'avez pas créé de compte sur HomeWork, vous pouvez ignorer cet email.%n%n" +
						"Cordialement,%n" +
						"L'équipe HomeWork",
				Optional.ofNullable(firstName).orElse(""),
				verificationUrl
		);

		sendEmail(to, "Vérifiez votre adresse email - HomeWork", body);
	}

	public void sendEnrollmentConfirmationEmail(User student, Course course) {
		if (student == null || course == null) {
			log.warn("Skipping enrollment confirmation email because student or course is null");
			return;
		}

		String subject = "Confirmation d'inscription - " + course.getTitle();
		String body = String.format(
				"Bonjour %s,%n%n" +
						"Votre inscription au cours \"%s\" a bien été confirmée.%n%n" +
						"Détails du cours :%n" +
						"- Date : %s%n" +
						"- Ville : %s%n" +
						"- Durée : %s%n" +
						"- Formateur : %s %s%n" +
						"- Montant réglé : %s%n%n" +
						"Nous vous souhaitons un excellent cours !%n%n" +
						"L'équipe HomeWork",
				Optional.ofNullable(student.getFirstName()).orElse(""),
				Optional.ofNullable(course.getTitle()).orElse(""),
				formatCourseDate(course.getCourseDateTime()),
				Optional.ofNullable(course.getCity()).orElse("à préciser"),
				formatDuration(course.getDuration()),
				Optional.ofNullable(course.getTeacher()).map(User::getFirstName).orElse(""),
				Optional.ofNullable(course.getTeacher()).map(User::getLastName).orElse(""),
				formatAmount(course.getPrice())
		);

		sendEmail(student.getEmail(), subject, body);
	}

	public void sendEnrollmentCancellationEmail(User student, Course course) {
		if (student == null || course == null) {
			log.warn("Skipping enrollment cancellation email because student or course is null");
			return;
		}

		String subject = "Annulation et remboursement - " + course.getTitle();
		String body = String.format(
				"Bonjour %s,%n%n" +
						"Votre inscription au cours \"%s\" a été annulée.%n" +
						"Un remboursement de %s a été initié et apparaîtra sur votre moyen de paiement sous quelques jours ouvrés.%n%n" +
						"Détails du cours annulé :%n" +
						"- Date : %s%n" +
						"- Ville : %s%n" +
						"- Formateur : %s %s%n%n" +
						"Si vous avez annulé par erreur, vous pouvez vous réinscrire dès maintenant depuis votre espace étudiant.%n%n" +
						"À bientôt sur HomeWork,%n" +
						"L'équipe HomeWork",
				Optional.ofNullable(student.getFirstName()).orElse(""),
				Optional.ofNullable(course.getTitle()).orElse(""),
				formatAmount(course.getPrice()),
				formatCourseDate(course.getCourseDateTime()),
				Optional.ofNullable(course.getCity()).orElse("à préciser"),
				Optional.ofNullable(course.getTeacher()).map(User::getFirstName).orElse(""),
				Optional.ofNullable(course.getTeacher()).map(User::getLastName).orElse("")
		);

		sendEmail(student.getEmail(), subject, body);
	}

	private void sendEmail(String to, String subject, String body) {
		if (to == null || to.isBlank()) {
			log.warn("Attempted to send email with empty recipient. Subject: {}", subject);
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);

		try {
			mailSender.send(message);
			log.info("Email '{}' sent successfully to {}", subject, to);
		} catch (MailException ex) {
			log.error("❌ FAILED to send email '{}' to {}: {}", subject, to, ex.getMessage(), ex);
			if (ex.getCause() != null) {
				log.error("Root cause: {}", ex.getCause().getMessage());
			}
		} catch (Exception ex) {
			log.error("❌ Unexpected error sending email '{}' to {}: {}", subject, to, ex.getMessage(), ex);
		}
	}

	private String formatCourseDate(LocalDateTime courseDateTime) {
		if (courseDateTime == null) {
			return "Date à préciser";
		}
		return COURSE_DATE_FORMAT.format(courseDateTime);
	}

	private String formatDuration(Integer durationInMinutes) {
		if (durationInMinutes == null || durationInMinutes <= 0) {
			return "à préciser";
		}
		return durationInMinutes + " minutes";
	}

	private String formatAmount(BigDecimal amount) {
		if (amount == null) {
			return "—";
		}
		return NumberFormat.getCurrencyInstance(Locale.FRANCE).format(amount);
	}
}

