package com.homework.backend.config;

import com.homework.backend.config.props.FrontendProperties;
import com.homework.backend.config.props.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, FrontendProperties.class})
public class AppConfig {}
