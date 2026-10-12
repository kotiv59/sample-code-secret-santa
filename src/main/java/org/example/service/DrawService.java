package org.example.service;

import org.example.entity.Participant;
import org.example.entity.RowParticipant;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrawService {
    public List<RowParticipant> draw(List<Participant> list) {
        if (list.size() < 2) {
            throw new IllegalArgumentException("List must contain at least 2 participants");
        }

        List<Participant> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);

        List<RowParticipant> result = new ArrayList<>();
        for (int i = 0; i < shuffled.size(); i++) {
            Participant giver = shuffled.get(i);
            Participant receiver = shuffled.get((i + 1) % shuffled.size());
            result.add(new RowParticipant(giver, receiver));
        }
        return result;
    }
}
