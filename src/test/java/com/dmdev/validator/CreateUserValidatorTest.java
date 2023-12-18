package com.dmdev.validator;

import com.dmdev.dto.CreateUserDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CreateUserValidatorTest {

    private final CreateUserValidator createUserValidator = CreateUserValidator.getInstance();

    @Test
    void validate() {
        var createUserDto = CreateUserDto.builder()
                .birthday("12-25-2002")
                .role("test")
                .gender("test")
                .build();

        var validationResult = createUserValidator.validate(createUserDto);
        var errors = validationResult.getErrors();

        List<String> codes = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        for (Error error : errors) {
            codes.add(error.getCode());
            messages.add(error.getMessage());
        }

        assertAll(
                () -> assertThat(codes).contains("invalid.birthday", "invalid.gender", "invalid.role"),
                () -> assertThat(messages).contains("Birthday is invalid", "Gender is invalid", "Role is invalid")
        );
    }

    @Test
    void trueIfNoErrors() {
        var createUserDto = CreateUserDto.builder()
                .birthday("2000-11-20")
                .gender("FEMALE")
                .role("ADMIN")
                .build();

        var validationResult = createUserValidator.validate(createUserDto);
        var actualResult = validationResult.getErrors().isEmpty();

        assertTrue(actualResult);
    }
}