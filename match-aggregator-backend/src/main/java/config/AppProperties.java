package ru.mirea.matchaggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Bootstrap bootstrap,
        Cors cors,
        boolean permanentDeleteEnabled
) {
    public record Jwt(String secretBase64, String issuer, int accessMinutes, int refreshDays) {}
    public record Bootstrap(String email, String name, String password) {}
    public record Cors(String allowedOrigins) {}
}
