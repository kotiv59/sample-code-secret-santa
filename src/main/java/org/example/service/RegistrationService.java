package org.example.service;

import org.example.config.SantaProperties;
import org.example.dto.ParticipantForm;
import org.example.entity.Participant;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class RegistrationService {
    private final ParticipantRepository participants;
    private final RowParticipantRepository rows;
    private final MailService mailService;
    private final SantaProperties properties;
    private final Clock clock;

    public RegistrationService(ParticipantRepository participants, RowParticipantRepository rows,
                               MailService mailService, SantaProperties properties, Clock clock) {
        this.participants = participants;
        this.rows = rows;
        this.mailService = mailService;
        this.properties = properties;
        this.clock = clock;
    }

    public boolean isDeadlinePassed() {
        return LocalDateTime.now(clock).isAfter(properties.registrationDeadline());
    }

    public boolean isDrawDone() {
        return rows.count() > 0;
    }

    public boolean isOpen() {
        return !isDeadlinePassed() && !isDrawDone();
    }

    /** Самостоятельная регистрация участника через сайт. */
    public Participant register(ParticipantForm form) {
        if (!isOpen()) {
            throw new GameException("Регистрация закрыта");
        }
        return save(form);
    }

    /** Добавление организатором: разрешено и после дедлайна, но только до жеребьёвки. */
    public Participant addByAdmin(ParticipantForm form) {
        if (isDrawDone()) {
            throw new GameException("Жеребьёвка уже проведена");
        }
        return save(form);
    }

    private Participant save(ParticipantForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (participants.existsByEmail(email)) {
            throw new GameException("Этот email уже зарегистрирован");
        }
        List<String> interests = form.getInterests().stream().distinct().toList();
        for (String interest : interests) {
            if (!properties.interests().contains(interest)) {
                throw new GameException("Неизвестный интерес: " + interest);
            }
        }
        Participant participant = participants.save(new Participant(
                form.getLastName().trim(),
                form.getFirstName().trim(),
                normalizeMiddleName(form.getMiddleName()),
                email,
                interests,
                LocalDateTime.now(clock)));
        mailService.sendRegistered(participant);
        return participant;
    }

    /** Отчество целиком или инициал: «с», «С», «с.» → «С.». */
    static String normalizeMiddleName(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String trimmed = s.trim();
        if (trimmed.matches("\\p{L}\\.?")) {
            return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + ".";
        }
        return trimmed;
    }
}
