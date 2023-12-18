package com.dmdev.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    private ValidationResult validationResult;

    @BeforeEach
    void prepare() {
        validationResult = new ValidationResult();
    }

    @Test
    void add() {
        Error error = Error.of("test code", "Test message");

        validationResult.add(error);
        List<Error> errors = validationResult.getErrors();
        boolean actualResult = errors.contains(error);

        assertTrue(actualResult);
    }

    @Test
    void isValid() {
        boolean actualResult = validationResult.isValid();

        assertTrue(actualResult);
    }

    @Test
    void hasErrors() {
        Error error = Error.of("test code", "Test message");

        validationResult.add(error);
        boolean actualResult = validationResult.hasErrors();

        assertTrue(actualResult);
    }
}