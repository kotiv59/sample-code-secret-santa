package org.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.config.SantaProperties;
import org.example.entity.Participant;
import org.example.entity.RowParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Отправка писем участникам. Ошибки SMTP не пробрасываются: они логируются,
 * чтобы сбой почты не ломал регистрацию и жеребьёвку.
 */
@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender sender;
    private final TemplateEngine templates;
    private final SantaProperties properties;
    private final NameFormatter names;

    public MailService(JavaMailSender sender, TemplateEngine templates, SantaProperties properties,
                       NameFormatter names) {
        this.sender = sender;
        this.templates = templates;
        this.properties = properties;
        this.names = names;
    }

    public void sendRegistered(Participant participant) {
        send(participant.getEmail(), properties.eventName() + ": ты в списке участников", "mail/registered",
                Map.of("santa", properties,
                        "greeting", names.greeting(participant)));
    }

    /** @return true, если письмо отправлено (или записано в лог при выключенной почте) */
    public boolean sendAssignment(RowParticipant row) {
        Participant giver = row.getGiver();
        Participant receiver = row.getReceiver();
        return send(giver.getEmail(), properties.eventName() + ": кому ты даришь подарок", "mail/assignment",
                Map.of("santa", properties,
                        "greeting", names.greeting(giver),
                        "joined", names.joined(giver),
                        "receiverDative", names.dative(receiver),
                        "receiver", receiver));
    }

    private boolean send(String to, String subject, String template, Map<String, Object> variables) {
        String html = templates.process(template, new Context(null, variables));
        if (!properties.mail().enabled()) {
            log.info("Почта выключена (santa.mail.enabled=false). Письмо для {}: {}\n{}", to, subject, html);
            return true;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(properties.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(message);
            log.info("Письмо «{}» отправлено на {}", subject, to);
            return true;
        } catch (MailException | MessagingException e) {
            log.error("Не удалось отправить письмо «{}» на {}", subject, to, e);
            return false;
        }
    }
}
