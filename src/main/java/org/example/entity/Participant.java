package org.example.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, length = 100)
    private String firstName;
    @Column(length = 100)
    private String middleName;
    @Column(nullable = false, unique = true)
    private String email;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<String> interests = new ArrayList<>();
    @Column(nullable = false)
    private LocalDateTime registeredAt;

    protected Participant() {
    }

    public Participant(String lastName, String firstName, String middleName, String email,
                       List<String> interests, LocalDateTime registeredAt) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.email = email;
        this.interests = new ArrayList<>(interests);
        this.registeredAt = registeredAt;
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getFullName() {
        return Stream.of(lastName, firstName, middleName)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    public String getEmail() {
        return email;
    }

    public List<String> getInterests() {
        return interests;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
