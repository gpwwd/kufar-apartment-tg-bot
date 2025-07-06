package com.innowise.aikufarbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kufar")
public class KufarApiConfig {
    private String baseUrl;
    private String currency;
    private String lowerPrice;
    private String upperPrice;
    private String checkInterval;
} 