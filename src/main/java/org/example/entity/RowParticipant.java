package org.example.entity;

import jakarta.persistence.*;

/**
 * Одна пара жеребьёвки: кто дарит и кому дарит.
 */
@Entity
public class RowParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Participant giver;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Participant receiver;
    private boolean mailSent;

    protected RowParticipant() {
    }

    public RowParticipant(Participant giver, Participant receiver) {
        this.giver = giver;
        this.receiver = receiver;
    }

    public Long getId() {
        return id;
    }

    public Participant getGiver() {
        return giver;
    }

    public Participant getReceiver() {
        return receiver;
    }

    public boolean isMailSent() {
        return mailSent;
    }

    public void setMailSent(boolean mailSent) {
        this.mailSent = mailSent;
    }
}
