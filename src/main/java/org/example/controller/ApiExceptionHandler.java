package org.example.controller;

import org.example.service.GameException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(GameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> game(GameException e) {
        return Map.of("error", e.getMessage());
    }
}
