package org.example.service;

import org.example.entity.Participant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameFormatterTest {
    private final NameFormatter names = new NameFormatter();

    private static Participant p(String last, String first, String middle) {
        return new Participant(last, first, middle, "x@mail.ru", List.of(), LocalDateTime.now());
    }

    @Test
    void dativeFirstAndLastName() {
        assertEquals("Олегу Мартынову", names.dative(p("Мартынов", "Олег", null)));
        assertEquals("Анне Петровой", names.dative(p("Петрова", "Анна", null)));
        assertEquals("Илье Котову", names.dative(p("Котов", "Илья", "Сергеевич")));
        assertEquals("Любови Черных", names.dative(p("Черных", "Любовь", "Ивановна")));
    }

    @Test
    void genderFromPatronymicFirstNameOrLastName() {
        assertEquals(NameFormatter.Gender.MALE, names.genderOf(p("Ким", "Саша", "Петрович")));
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Ким", "Саша", "Петровна")));
        assertEquals(NameFormatter.Gender.MALE, names.genderOf(p("Котов", "Илья", null)));
        assertEquals(NameFormatter.Gender.MALE, names.genderOf(p("Черных", "Никита", null)));
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Черных", "Мария", null)));
        assertEquals(NameFormatter.Gender.MALE, names.genderOf(p("Черных", "Олег", null)));
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Черных", "Любовь", null)));
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Иванова", "Женя", null)));
        assertEquals(NameFormatter.Gender.UNKNOWN, names.genderOf(p("Ким", "Саша", null)));
        // отчество инициалом — пол определяется по имени, затем по фамилии
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Петрова", "Анна", "С.")));
        assertEquals(NameFormatter.Gender.MALE, names.genderOf(p("Котов", "Илья", "С.")));
        assertEquals(NameFormatter.Gender.FEMALE, names.genderOf(p("Иванова", "Саша", "А.")));
        assertEquals("Анне Петровой", names.dative(p("Петрова", "Анна", "С.")));
    }

    @Test
    void greetingAndJoinedPhraseDependOnGender() {
        assertEquals("Дорогой Илья", names.greeting(p("Котов", "Илья", null)));
        assertEquals("Дорогая Анна", names.greeting(p("Петрова", "Анна", null)));
        assertEquals("Привет, Саша", names.greeting(p("Ким", "Саша", null)));
        assertEquals("Ты стал частью", names.joined(p("Котов", "Илья", null)));
        assertEquals("Ты стала частью", names.joined(p("Петрова", "Анна", null)));
        assertEquals("Теперь ты — часть", names.joined(p("Ким", "Саша", null)));
    }
}
