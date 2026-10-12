package org.example.controller;

import jakarta.validation.Valid;
import org.example.config.SantaProperties;
import org.example.dto.ParticipantForm;
import org.example.dto.ParticipantView;
import org.example.entity.Participant;
import org.example.repository.ParticipantRepository;
import org.example.service.GameException;
import org.example.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
public class HomeController {
    static final DateTimeFormatter DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.of("ru"));

    private final RegistrationService registration;
    private final ParticipantRepository participants;
    private final SantaProperties properties;
    private final Clock clock;

    public HomeController(RegistrationService registration, ParticipantRepository participants,
                          SantaProperties properties, Clock clock) {
        this.registration = registration;
        this.participants = participants;
        this.properties = properties;
        this.clock = clock;
    }

    @GetMapping("/")
    public String index(Model model) {
        addGameState(model);
        model.addAttribute("deadlineShort", deadlineShort());
        model.addAttribute("timeLeft", timeLeft());
        model.addAttribute("participants", participants.findAllByOrderByRegisteredAtAsc().stream()
                .map(ParticipantView::from)
                .toList());
        return "index";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        addGameState(model);
        model.addAttribute("form", new ParticipantForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") ParticipantForm form, BindingResult errors,
                           Model model, RedirectAttributes redirect) {
        addGameState(model);
        if (errors.hasErrors()) {
            return "register";
        }
        try {
            Participant participant = registration.register(form);
            redirect.addFlashAttribute("email", participant.getEmail());
            return "redirect:/register/done";
        } catch (GameException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/register/done")
    public String done(Model model) {
        model.addAttribute("santa", properties);
        return "register-done";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private void addGameState(Model model) {
        model.addAttribute("santa", properties);
        model.addAttribute("deadline", DEADLINE_FORMAT.format(properties.registrationDeadline()));
        model.addAttribute("open", registration.isOpen());
        model.addAttribute("drawDone", registration.isDrawDone());
        model.addAttribute("allInterests", properties.interests());
    }

    /** «20.11.2026 (включительно)», если регистрация до конца дня, иначе «20.11.2026 18:00». */
    private String deadlineShort() {
        LocalDateTime deadline = properties.registrationDeadline();
        String date = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(deadline);
        if (deadline.toLocalTime().isAfter(java.time.LocalTime.of(23, 58))) {
            return date + " (включительно)";
        }
        return date + " " + DateTimeFormatter.ofPattern("HH:mm").format(deadline);
    }

    /** «осталось 5 дней» / «осталось 3 часа»; null, если дедлайн прошёл. */
    private String timeLeft() {
        Duration left = Duration.between(LocalDateTime.now(clock), properties.registrationDeadline());
        if (left.isNegative()) {
            return null;
        }
        long days = left.toDays();
        if (days > 0) {
            return "осталось " + days + " " + plural(days, "день", "дня", "дней");
        }
        long hours = Math.max(1, left.toHours());
        return "осталось " + hours + " " + plural(hours, "час", "часа", "часов");
    }

    static String plural(long n, String one, String few, String many) {
        long mod100 = n % 100;
        long mod10 = n % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return many;
        }
        if (mod10 == 1) {
            return one;
        }
        if (mod10 >= 2 && mod10 <= 4) {
            return few;
        }
        return many;
    }
}
