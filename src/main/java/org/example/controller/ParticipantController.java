package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.ParticipantForm;
import org.example.dto.ParticipantView;
import org.example.entity.Participant;
import org.example.repository.ParticipantRepository;
import org.example.service.GameService;
import org.example.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participant")
public class ParticipantController {
    private final ParticipantRepository repository;
    private final RegistrationService registration;
    private final GameService game;

    public ParticipantController(ParticipantRepository repository, RegistrationService registration,
                                 GameService game) {
        this.repository = repository;
        this.registration = registration;
        this.game = game;
    }

    @PostMapping
    public Participant add(@Valid @RequestBody ParticipantForm form) {
        return registration.addByAdmin(form);
    }

    @GetMapping
    public List<ParticipantView> list() {
        return repository.findAllByOrderByRegisteredAtAsc().stream()
                .map(ParticipantView::from)
                .toList();
    }

    @DeleteMapping
    public void clear() {
        game.reset();
        repository.deleteAll();
    }
}
