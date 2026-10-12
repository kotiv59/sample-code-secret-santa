package org.example;

import org.example.dto.ParticipantForm;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.*;
import java.util.List;

public final class TestSupport {
    private TestSupport() {
    }

    public static ParticipantForm form(String lastName, String email, String... interests) {
        ParticipantForm form = new ParticipantForm();
        form.setLastName(lastName);
        form.setFirstName("Иван");
        form.setMiddleName("Иванович");
        form.setEmail(email);
        form.setInterests(List.of(interests));
        return form;
    }

    /** Часы, которые тест может переставить. */
    public static class MutableClock extends Clock {
        private Instant now;

        public MutableClock(LocalDateTime time) {
            set(time);
        }

        public void set(LocalDateTime time) {
            this.now = time.atZone(ZoneId.systemDefault()).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /** Дедлайн в тестовом конфиге — 2026-12-20T23:59; по умолчанию «сейчас» 1 декабря. */
    public static final LocalDateTime BEFORE_DEADLINE = LocalDateTime.of(2026, 12, 1, 12, 0);
    public static final LocalDateTime AFTER_DEADLINE = LocalDateTime.of(2026, 12, 21, 0, 0);

    @TestConfiguration
    public static class ClockConfig {
        @Bean
        @Primary
        public MutableClock testClock() {
            return new MutableClock(BEFORE_DEADLINE);
        }
    }
}
