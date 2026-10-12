package org.example.service;

import org.example.TestSupport;
import org.example.TestSupport.MutableClock;
import org.example.entity.Participant;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.example.TestSupport.form;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestSupport.ClockConfig.class)
class RegistrationServiceTest {
    @Autowired
    RegistrationService registration;
    @Autowired
    GameService game;
    @Autowired
    ParticipantRepository participants;
    @Autowired
    RowParticipantRepository rows;
    @Autowired
    MutableClock clock;

    @BeforeEach
    void clean() {
        rows.deleteAll();
        participants.deleteAll();
        clock.set(TestSupport.BEFORE_DEADLINE);
    }

    @Test
    void registersAndNormalizesInput() {
        var f = form(" Иванов ", " Ivan@Mail.RU ", "Машины", "Кухня");
        f.setMiddleName("  ");

        Participant p = registration.register(f);

        assertEquals("Иванов", p.getLastName());
        assertEquals("ivan@mail.ru", p.getEmail());
        assertNull(p.getMiddleName());
        assertEquals(List.of("Машины", "Кухня"), p.getInterests());
        assertEquals(TestSupport.BEFORE_DEADLINE, p.getRegisteredAt());
        assertEquals(1, participants.count());
    }

    @Test
    void middleNameInitialIsNormalized() {
        assertEquals("С.", RegistrationService.normalizeMiddleName(" с "));
        assertEquals("С.", RegistrationService.normalizeMiddleName("с."));
        assertEquals("С.", RegistrationService.normalizeMiddleName("С."));
        assertEquals("Сергеевна", RegistrationService.normalizeMiddleName(" Сергеевна "));
        assertNull(RegistrationService.normalizeMiddleName("  "));

        var f = form("Петрова", "anna@mail.ru");
        f.setFirstName("Анна");
        f.setMiddleName("с");
        assertEquals("Петрова Анна С.", registration.register(f).getFullName());
    }

    @Test
    void rejectsAfterDeadline() {
        clock.set(TestSupport.AFTER_DEADLINE);

        var e = assertThrows(GameException.class, () -> registration.register(form("Иванов", "ivan@mail.ru")));
        assertEquals("Регистрация закрыта", e.getMessage());
        assertEquals(0, participants.count());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndSpaces() {
        registration.register(form("Иванов", "ivan@mail.ru"));

        var e = assertThrows(GameException.class,
                () -> registration.register(form("Петров", "Ivan@Mail.ru ")));
        assertEquals("Этот email уже зарегистрирован", e.getMessage());
    }

    @Test
    void rejectsUnknownInterest() {
        var e = assertThrows(GameException.class,
                () -> registration.register(form("Иванов", "ivan@mail.ru", "Хакерство")));
        assertEquals("Неизвестный интерес: Хакерство", e.getMessage());
    }

    @Test
    void adminCanAddAfterDeadline() {
        clock.set(TestSupport.AFTER_DEADLINE);

        registration.addByAdmin(form("Иванов", "ivan@mail.ru"));

        assertEquals(1, participants.count());
    }

    @Test
    void nobodyCanAddAfterDraw() {
        registration.register(form("Иванов", "ivan@mail.ru"));
        registration.register(form("Петров", "petr@mail.ru"));
        game.runDraw();

        assertFalse(registration.isOpen());
        var e = assertThrows(GameException.class,
                () -> registration.addByAdmin(form("Сидоров", "sid@mail.ru")));
        assertEquals("Жеребьёвка уже проведена", e.getMessage());
        assertThrows(GameException.class, () -> registration.register(form("Сидоров", "sid@mail.ru")));
    }
}
