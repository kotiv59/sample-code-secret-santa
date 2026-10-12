package org.example.service;

import org.example.entity.Participant;
import org.example.entity.RowParticipant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DrawServiceTest {
    private final DrawService service = new DrawService();

    private static List<Participant> participants(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> new Participant("Фамилия" + i, "Имя" + i, null, "p" + i + "@mail.ru",
                        List.of(), LocalDateTime.now()))
                .toList();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void everyoneGivesAndReceivesExactlyOnceAndNotToSelf(int n) {
        List<Participant> list = participants(n);

        List<RowParticipant> rows = service.draw(list);

        assertEquals(n, rows.size());
        rows.forEach(r -> assertNotSame(r.getGiver(), r.getReceiver()));
        Set<Participant> givers = rows.stream().map(RowParticipant::getGiver).collect(Collectors.toSet());
        Set<Participant> receivers = rows.stream().map(RowParticipant::getReceiver).collect(Collectors.toSet());
        assertEquals(new HashSet<>(list), givers);
        assertEquals(new HashSet<>(list), receivers);
    }

    @Test
    void lessThanTwoParticipantsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.draw(participants(1)));
    }
}
