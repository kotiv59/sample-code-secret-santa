package org.example.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.TestSupport;
import org.example.entity.RowParticipant;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.List;

import static org.example.TestSupport.form;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "santa.mail.enabled=true")
@Import(TestSupport.ClockConfig.class)
class GameServiceIT {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    RegistrationService registration;
    @Autowired
    GameService game;
    @Autowired
    ParticipantRepository participants;
    @Autowired
    RowParticipantRepository rows;
    @Autowired
    NameFormatter names;

    @BeforeEach
    void clean() {
        rows.deleteAll();
        participants.deleteAll();
    }

    @Test
    void registrationAndDrawSendLetters() throws Exception {
        registration.register(form("Иванов", "ivan@mail.ru", "Машины"));
        registration.register(form("Петров", "petr@mail.ru", "Кухня"));
        registration.register(form("Сидоров", "sid@mail.ru", "Готовка"));
        assertEquals(3, greenMail.getReceivedMessages().length);

        List<RowParticipant> pairs = game.runDraw();

        MimeMessage[] all = greenMail.getReceivedMessages();
        assertEquals(6, all.length);
        for (RowParticipant row : pairs) {
            assertTrue(row.isMailSent());
            MimeMessage letter = find(all, row.getGiver().getEmail(), "Operation Secret Santa: кому ты даришь подарок");
            String html = (String) letter.getContent();
            assertTrue(html.contains("Тебе предстоит подарить подарок <b>" + names.dative(row.getReceiver()) + "</b>"), html);
            assertTrue(html.contains("Дорогой Иван"), html);
            assertTrue(html.contains("Ты стал частью"), html);
            assertTrue(html.contains("1900"), html);
            assertTrue(html.contains("<span>26.12.2026, первая половина дня</span>"), html);
            assertTrue(html.contains("Кухня"), html);
        }
    }

    @Test
    void secondDrawIsRejectedAndPairsStayTheSame() {
        registration.register(form("Иванов", "ivan@mail.ru"));
        registration.register(form("Петров", "petr@mail.ru"));
        List<Long> first = game.runDraw().stream().map(RowParticipant::getId).toList();

        var e = assertThrows(GameException.class, () -> game.runDraw());
        assertEquals("Жеребьёвка уже проведена", e.getMessage());
        assertEquals(first, game.pairs().stream().map(RowParticipant::getId).toList());
    }

    @Test
    void drawNeedsTwoParticipants() {
        registration.register(form("Иванов", "ivan@mail.ru"));

        var e = assertThrows(GameException.class, () -> game.runDraw());
        assertEquals("Нужно минимум 2 участника", e.getMessage());
    }

    private static MimeMessage find(MimeMessage[] all, String to, String subject)
            throws MessagingException, IOException {
        for (MimeMessage m : all) {
            if (m.getSubject().equals(subject) && m.getAllRecipients()[0].toString().equals(to)) {
                return m;
            }
        }
        throw new AssertionError("Нет письма «" + subject + "» для " + to);
    }
}
