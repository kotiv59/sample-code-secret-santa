package org.example.service;

import org.example.TestSupport;
import org.example.entity.RowParticipant;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.example.TestSupport.form;
import static org.junit.jupiter.api.Assertions.*;

/** На порту 3999 никто не слушает — каждая отправка письма падает. */
@SpringBootTest(properties = {"santa.mail.enabled=true", "spring.mail.port=3999"})
@Import(TestSupport.ClockConfig.class)
class BrokenSmtpIT {
    @Autowired
    RegistrationService registration;
    @Autowired
    GameService game;
    @Autowired
    ParticipantRepository participants;
    @Autowired
    RowParticipantRepository rows;

    @Test
    void mailFailuresDoNotBreakRegistrationOrDraw() {
        rows.deleteAll();
        participants.deleteAll();

        registration.register(form("Иванов", "ivan@mail.ru"));
        registration.register(form("Петров", "petr@mail.ru"));
        List<RowParticipant> pairs = game.runDraw();

        assertEquals(2, participants.count());
        assertTrue(pairs.stream().noneMatch(RowParticipant::isMailSent));
        assertEquals(2, rows.findAllByMailSentFalse().size());
        assertEquals(0, game.resendFailed());
    }
}
