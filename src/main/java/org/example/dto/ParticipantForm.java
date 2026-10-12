package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Данные формы регистрации (и формы «Добавить участника» в админке).
 */
public class ParticipantForm {
    @NotBlank(message = "Укажите фамилию")
    @Size(max = 100, message = "Не длиннее 100 символов")
    private String lastName;

    @NotBlank(message = "Укажите имя")
    @Size(max = 100, message = "Не длиннее 100 символов")
    private String firstName;

    @Size(max = 100, message = "Не длиннее 100 символов")
    private String middleName;

    @NotBlank(message = "Укажите email")
    @Email(message = "Некорректный email")
    private String email;

    private List<String> interests = new ArrayList<>();

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? new ArrayList<>() : interests;
    }
}
