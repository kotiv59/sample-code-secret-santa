package org.example.service;

import org.example.entity.Participant;
import org.example.entity.RowParticipant;
import org.example.repository.ParticipantRepository;
import org.example.repository.RowParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {
    private final ParticipantRepository participants;
    private final RowParticipantRepository rows;
    private final DrawService drawService;
    private final MailService mailService;

    public GameService(ParticipantRepository participants, RowParticipantRepository rows,
                       DrawService drawService, MailService mailService) {
        this.participants = participants;
        this.rows = rows;
        this.drawService = drawService;
        this.mailService = mailService;
    }

    public List<RowParticipant> pairs() {
        return rows.findAll();
    }

    /** Проводит жеребьёвку и рассылает каждому дарителю письмо с его получателем. */
    public synchronized List<RowParticipant> runDraw() {
        if (rows.count() > 0) {
            throw new GameException("Жеребьёвка уже проведена");
        }
        List<Participant> list = participants.findAllByOrderByRegisteredAtAsc();
        if (list.size() < 2) {
            throw new GameException("Нужно минимум 2 участника");
        }
        List<RowParticipant> saved = rows.saveAll(drawService.draw(list));
        sendAll(saved);
        return saved;
    }

    /** Повторно отправляет письма тем, кому они не дошли. @return сколько отправлено успешно */
    public synchronized int resendFailed() {
        List<RowParticipant> failed = rows.findAllByMailSentFalse();
        sendAll(failed);
        return (int) failed.stream().filter(RowParticipant::isMailSent).count();
    }

    public synchronized void reset() {
        rows.deleteAll();
    }

    public synchronized void deleteParticipant(Long id) {
        if (rows.count() > 0) {
            throw new GameException("Нельзя удалить участника после жеребьёвки — сначала сбросьте её");
        }
        participants.deleteById(id);
    }

    private void sendAll(List<RowParticipant> list) {
        for (RowParticipant row : list) {
            row.setMailSent(mailService.sendAssignment(row));
        }
        rows.saveAll(list);
    }
}
