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
        int minimalBudget,
        int maxBudget,
        LocalDate giftDate,
        String giftTime,
        String giftPlace,
        String organizerName,
        String organizerEmail,
        String organizerContact,
        String siteUrl,
        List<String> interests,
        Admin admin,
        Mail mail) {

    /** «от 1000 до 1500 рублей» — одна формулировка бюджета для сайта и писем */
    public String budgetText() {
        return "от " + minimalBudget + " до " + maxBudget + " рублей";
    }

    public record Admin(String username, String password) {
    }

    public record Mail(boolean enabled, String from, String inviteTo) {
    }
}
