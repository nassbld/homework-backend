package com.homework.backend.services;

import com.homework.backend.dto.LoginRequest;
import com.homework.backend.dto.RegisterRequest;
import com.homework.backend.models.EmailVerificationToken;
import com.homework.backend.models.User;
import com.homework.backend.repositories.EmailVerificationTokenRepository;
import com.homework.backend.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService, EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public User register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        User user = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(registerRequest.role())
                .verifiedEmail(false)
                .build();

        User savedUser = userRepository.save(user);

        // Generate verification token and send email
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(savedUser)
                .used(false)
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token, savedUser.getFirstName());

        return savedUser;
    }

    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe invalide."));

        if (!user.getVerifiedEmail()) {
            throw new IllegalStateException("Veuillez vérifier votre adresse email avant de vous connecter.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        return jwtService.generateToken(user);
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de vérification invalide."));

        User user = verificationToken.getUser();
        
        // If token is already used but user is verified, consider it success
        if (verificationToken.getUsed() && user.getVerifiedEmail()) {
            // User is already verified, this is a duplicate request - treat as success
            return;
        }
        
        // If token is used but user is NOT verified, something went wrong
        if (verificationToken.getUsed()) {
            throw new IllegalStateException("Ce token a déjà été utilisé.");
        }

        if (verificationToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Ce token a expiré.");
        }

        // Verify the user
        user.setVerifiedEmail(true);
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }
}