package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ConfigurationProperties(prefix = "santa")
public record SantaProperties(
        String eventName,
        LocalDateTime registrationDeadline,
        LocalDate drawDate,
        int budget,
        LocalDate giftDate,
        String giftTime,
        String giftPlace,
        String organizerName,
        String organizerEmail,
        String organizerContact,
        List<String> interests,
        Admin admin,
        Mail mail) {

    public record Admin(String username, String password) {
    }

    public record Mail(boolean enabled, String from) {
    }
}
