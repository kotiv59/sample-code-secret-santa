package org.example.controller;

import org.example.entity.RowParticipant;
import org.example.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/draw")
public class DrawController {
    private final GameService game;

    public DrawController(GameService game) {
        this.game = game;
    }

    /** Результат жеребьёвки: кто кому дарит (пустой список, если её ещё не было). */
    @GetMapping
    public List<RowParticipant> pairs() {
        return game.pairs();
    }

    @PostMapping
    public List<RowParticipant> startGame() {
        return game.runDraw();
    }
}
