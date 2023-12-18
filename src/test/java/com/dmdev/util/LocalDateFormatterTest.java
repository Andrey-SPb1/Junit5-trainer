package com.dmdev.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateFormatterTest {

    @Test
    void format() {
        String date = "1998-07-25";
        LocalDate expectedDate = LocalDate.of(1998, 7, 25);

        var actualResult = LocalDateFormatter.format(date);

        assertEquals(expectedDate, actualResult);
    }

    @ParameterizedTest
    @MethodSource("getDateWithException")
    void throwsExceptionsIfDateIsNotCorrect(Class <? extends Throwable> exception, String date) {
        assertThrows(exception, () -> LocalDateFormatter.format(date));
    }

    @DisplayName("format date param")
    static Stream<Arguments> getDateWithException() {
        return Stream.of(
                Arguments.of(DateTimeParseException.class, "01-01-2000"),
                Arguments.of(DateTimeParseException.class, "2000-30-01"),
                Arguments.of(DateTimeParseException.class, "2000-01-01 19:12"),
                Arguments.of(DateTimeParseException.class, ""),
                Arguments.of(NullPointerException.class, null)
        );
    }
    @Test
    void isValid() {
        String date = "1998-07-25";

        var actualResult = LocalDateFormatter.isValid(date);

        assertTrue(actualResult);
    }

    @ParameterizedTest
    @MethodSource("getDate")
    void falseIfDateIsNotCorrect(String date) {
        assertFalse(LocalDateFormatter.isValid(date));
    }

    @DisplayName("is valid date param")
    static Stream<Arguments> getDate() {
        return Stream.of(
                Arguments.of("01-01-2000"),
                Arguments.of("2000-30-01"),
                Arguments.of("2000-01-01 19:12"),
                Arguments.of("")
        );
    }
}