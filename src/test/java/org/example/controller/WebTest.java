package org.example.controller;

import org.example.TestSupport;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.example.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.TestSupport.form;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSupport.ClockConfig.class)
class WebTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    RegistrationService registration;
    @Autowired
    ParticipantRepository participants;
    @Autowired
    RowParticipantRepository rows;
    @Autowired
    TestSupport.MutableClock clock;
    @Autowired
    org.example.service.GameService game;

    @BeforeEach
    void clean() {
        rows.deleteAll();
        participants.deleteAll();
        clock.set(TestSupport.BEFORE_DEADLINE);
    }

    @Test
    void homeShowsDeadlineAndParticipantsWithoutEmail() throws Exception {
        registration.register(form("Иванов", "ivan@mail.ru", "Машины"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20 декабря 2026, 23:59")))
                .andExpect(content().string(containsString("осталось 19 дней")))
                .andExpect(content().string(containsString("20.12.2026 (включительно)")))
                .andExpect(content().string(containsString("от 1000 до 1500 рублей")))
                .andExpect(content().string(containsString("22.12.2026")))
                .andExpect(content().string(containsString("Иванов Иван Иванович")))
                .andExpect(content().string(containsString("Машины")))
                .andExpect(content().string(not(containsString("ivan@mail.ru"))));
    }

    @Test
    void homeAfterDeadlineShowsOrganizerContact() throws Exception {
        clock.set(TestSupport.AFTER_DEADLINE);

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Регистрация закрыта")))
                .andExpect(content().string(containsString("organizer@example.com")))
                .andExpect(content().string(containsString("Telegram @organizer")));
    }

    @Test
    void invalidFormShowsErrors() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("lastName", "")
                        .param("firstName", "Иван")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Укажите фамилию")))
                .andExpect(content().string(containsString("Некорректный email")));
        assertEquals(0, participants.count());
    }

    @Test
    void validFormRegistersAndRedirects() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("lastName", "Иванов")
                        .param("firstName", "Иван")
                        .param("email", "ivan@mail.ru")
                        .param("interests", "Машины", "Кухня"))
                .andExpect(redirectedUrl("/register/done"));
        assertEquals(1, participants.count());
    }

    @Test
    void lateSubmitIsRejected() throws Exception {
        clock.set(TestSupport.AFTER_DEADLINE);

        mvc.perform(post("/register").with(csrf())
                        .param("lastName", "Иванов")
                        .param("firstName", "Иван")
                        .param("email", "ivan@mail.ru"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Регистрация закрыта")));
        assertEquals(0, participants.count());
    }

    @Test
    void adminRequiresLogin() throws Exception {
        mvc.perform(get("/admin")).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void adminSeesEmailsAndCanAddAfterDeadline() throws Exception {
        registration.register(form("Иванов", "ivan@mail.ru"));
        clock.set(TestSupport.AFTER_DEADLINE);

        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ivan@mail.ru")));

        mvc.perform(post("/admin/participants").with(csrf()).with(user("admin").roles("ADMIN"))
                        .param("lastName", "Петров")
                        .param("firstName", "Пётр")
                        .param("email", "petr@mail.ru"))
                .andExpect(redirectedUrl("/admin"));
        assertEquals(2, participants.count());
    }

    @Test
    void pairsAreHiddenUntilAdminRevealsThem() throws Exception {
        registration.register(form("Иванов", "ivan@mail.ru"));
        registration.register(form("Петров", "petr@mail.ru"));
        game.runDraw();

        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Писем доставлено")))
                .andExpect(content().string(containsString("Показать пары (экстренно)")))
                .andExpect(content().string(not(containsString("Кому дарит"))));

        mvc.perform(get("/admin").param("reveal", "true").with(user("admin").roles("ADMIN")))
                .andExpect(content().string(containsString("Кому дарит")))
                .andExpect(content().string(containsString("Скрыть пары")));
    }

    @Test
    void adminCanPreviewAndSendInvitation() throws Exception {
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(content().string(containsString("Отправить приглашение")))
                .andExpect(content().string(containsString("all@example.com")));

        mvc.perform(get("/admin/invitation").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Всем привет")))
                .andExpect(content().string(containsString("http://santa.example.com")))
                .andExpect(content().string(containsString("src=\"/img/operation-secret-santa.png\"")));

        mvc.perform(get("/img/operation-secret-santa.png")).andExpect(status().isOk());

        mvc.perform(post("/admin/invitation").with(csrf()).with(user("admin").roles("ADMIN")))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute("message", "Приглашение отправлено на all@example.com"));
    }

    @Test
    void invitationIsAdminOnly() throws Exception {
        mvc.perform(get("/admin/invitation")).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void drawApiIsProtectedAndParticipantApiHidesEmail() throws Exception {
        registration.register(form("Иванов", "ivan@mail.ru"));

        mvc.perform(get("/api/draw")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/participant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Иванов Иван Иванович"))
                .andExpect(content().string(not(containsString("ivan@mail.ru"))));
    }
}
