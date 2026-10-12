package org.example.service;

import com.github.petrovich4j.Case;
import com.github.petrovich4j.NameType;
import com.github.petrovich4j.Petrovich;
import org.example.entity.Participant;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Склонение имён и обращения в письмах с учётом пола.
 * Пол определяется по отчеству, затем по имени, затем по фамилии.
 */
@Component
public class NameFormatter {
    public enum Gender { MALE, FEMALE, UNKNOWN }

    /** Мужские имена на -а/-я. */
    private static final Set<String> MALE_ON_A = Set.of(
            "илья", "никита", "кузьма", "фома", "лука", "савва", "данила", "гаврила", "фока", "иона");
    /** Имена, которые носят и мужчины, и женщины. */
    private static final Set<String> UNISEX = Set.of(
            "саша", "женя", "валя", "шура", "саня", "сеня", "слава", "стася");
    /** Женские имена на мягкий знак (мужских на -ь больше: Игорь, Василь...). */
    private static final Set<String> FEMALE_ON_SOFT_SIGN = Set.of(
            "любовь", "нинель", "адель", "рахиль", "эсфирь", "юдифь", "агарь");

    private final Petrovich petrovich = new Petrovich();

    public Gender genderOf(Participant p) {
        Gender byPatronymic = byPatronymic(p.getMiddleName());
        if (byPatronymic != Gender.UNKNOWN) {
            return byPatronymic;
        }
        Gender byFirstName = byFirstName(p.getFirstName());
        if (byFirstName != Gender.UNKNOWN) {
            return byFirstName;
        }
        return byLastName(p.getLastName());
    }

    /** «Олегу Мартынову» — имя и фамилия в дательном падеже. */
    public String dative(Participant p) {
        com.github.petrovich4j.Gender g = switch (genderOf(p)) {
            case MALE -> com.github.petrovich4j.Gender.Male;
            case FEMALE -> com.github.petrovich4j.Gender.Female;
            case UNKNOWN -> com.github.petrovich4j.Gender.Both;
        };
        return petrovich.say(p.getFirstName(), NameType.FirstName, g, Case.Dative)
                + " " + petrovich.say(p.getLastName(), NameType.LastName, g, Case.Dative);
    }

    public String greeting(Participant p) {
        return switch (genderOf(p)) {
            case MALE -> "Дорогой " + p.getFirstName();
            case FEMALE -> "Дорогая " + p.getFirstName();
            case UNKNOWN -> "Привет, " + p.getFirstName();
        };
    }

    /** Начало фразы «… нашего волшебного обмена подарками». */
    public String joined(Participant p) {
        return switch (genderOf(p)) {
            case MALE -> "Ты стал частью";
            case FEMALE -> "Ты стала частью";
            case UNKNOWN -> "Теперь ты — часть";
        };
    }

    private static Gender byPatronymic(String middleName) {
        String s = normalize(middleName);
        if (s.endsWith("вич") || s.endsWith("ич") || s.endsWith("оглы")) {
            return Gender.MALE;
        }
        if (s.endsWith("вна") || s.endsWith("чна") || s.endsWith("кызы")) {
            return Gender.FEMALE;
        }
        return Gender.UNKNOWN;
    }

    private static Gender byFirstName(String firstName) {
        String s = normalize(firstName);
        if (s.isEmpty() || UNISEX.contains(s)) {
            return Gender.UNKNOWN;
        }
        if (MALE_ON_A.contains(s)) {
            return Gender.MALE;
        }
        if (FEMALE_ON_SOFT_SIGN.contains(s)) {
            return Gender.FEMALE;
        }
        if (s.endsWith("а") || s.endsWith("я")) {
            return Gender.FEMALE;
        }
        if (s.endsWith("ь")) {
            return Gender.UNKNOWN;
        }
        return Gender.MALE;
    }

    private static Gender byLastName(String lastName) {
        String s = normalize(lastName);
        if (s.matches(".*(ова|ева|ёва|ина|ына|ская|цкая|ая)$")) {
            return Gender.FEMALE;
        }
        if (s.matches(".*(ов|ев|ёв|ин|ын|ский|цкий|ой|ый|ий)$")) {
            return Gender.MALE;
        }
        return Gender.UNKNOWN;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
