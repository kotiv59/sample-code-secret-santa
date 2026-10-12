package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RulesController {
    public static final String RULES = """
            Secret Santa - это популярная игра, в которой группа людей анонимно обменивается подарками.
            Суть в том, что каждый участник по жребию получает имя другого игрока и готовит для него подарок,
            при этом сам остаётся анонимным.
            """;

    @GetMapping
    public String rules() {
        return RULES;
    }
}
