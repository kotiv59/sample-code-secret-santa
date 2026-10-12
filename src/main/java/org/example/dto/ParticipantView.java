package org.example.dto;

import org.example.entity.Participant;

import java.util.List;

/**
 * Публичное представление участника — без email.
 */
public record ParticipantView(String fullName, List<String> interests) {
    public static ParticipantView from(Participant p) {
        return new ParticipantView(p.getFullName(), List.copyOf(p.getInterests()));
    }
}
