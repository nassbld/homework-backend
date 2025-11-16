package com.homework.backend.config;

import com.homework.backend.config.props.StripeProperties;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    private final StripeProperties stripeProperties;

    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("✅ Stripe initialisé en mode : {}", stripeProperties.getSecretKey().startsWith("sk_test") ? "TEST" : "PRODUCTION");
    }
}
