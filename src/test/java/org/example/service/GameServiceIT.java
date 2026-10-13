package org.example.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
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
    @Autowired
    MailService mail;

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
            String html = htmlWithLogo(letter);
            assertTrue(html.contains("Тебе предстоит подарить подарок <b>" + names.dative(row.getReceiver()) + "</b>"), html);
            assertTrue(html.contains("Дорогой Иван"), html);
            assertTrue(html.contains("Ты стал частью"), html);
            assertTrue(html.contains("от 1000 до 1500 рублей"), html);
            assertTrue(html.contains("<span>26.12.2026, первая половина дня</span>"), html);
            assertTrue(html.contains("Кухня"), html);
        }
    }

    @Test
    void invitationGoesToMailingList() throws Exception {
        assertTrue(mail.sendInvitation());

        MimeMessage letter = find(greenMail.getReceivedMessages(), "all@example.com", "Operation Secret Santa");
        String html = htmlWithLogo(letter);
        assertTrue(html.contains("Всем привет"), html);
        assertTrue(html.contains("<b>от 1000 до 1500 рублей</b>"), html);
        assertTrue(html.contains("http://santa.example.com"), html);
        assertTrue(html.contains("<b>20.12.2026</b> (включительно)"), html);
        assertTrue(html.contains("<b>22.12.2026</b>"), html);
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

    /** Проверяет концовку письма (вложенный логотип, затем подпись) и возвращает HTML письма */
    private static String htmlWithLogo(MimeMessage letter) throws MessagingException, IOException {
        String raw = GreenMailUtil.getWholeMessage(letter);
        assertTrue(raw.contains("Content-ID: <logo>"), "картинка не вложена");
        assertTrue(raw.contains("image/png"), "картинка не вложена");

        MimeMultipart related = (MimeMultipart) ((MimeMultipart) letter.getContent()).getBodyPart(0).getContent();
        String html = (String) related.getBodyPart(0).getContent();
        assertTrue(html.contains("src=\"cid:logo\""), html);
        assertTrue(html.contains("Коллектив Сономы"), html);
        assertTrue(html.indexOf("cid:logo") < html.indexOf("С уважением"), "логотип должен быть перед подписью");
        return html;
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
