package com.dmdev.mapper;

import com.dmdev.dto.CreateUserDto;
import com.dmdev.entity.Gender;
import com.dmdev.entity.Role;
import com.dmdev.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserMapperTest {

    private final CreateUserMapper createUserMapper = CreateUserMapper.getInstance();

    @Test
    void map() {
        var createUserDto = CreateUserDto.builder()
                .name("Ivan")
                .birthday("2000-01-01")
                .email("ivan@gmail.com")
                .password("123")
                .role("ADMIN")
                .gender("MALE")
                .build();

        var expectedUser = User.builder()
                .name("Ivan")
                .birthday(LocalDate.of(2000, 1, 1))
                .email("ivan@gmail.com")
                .password("123")
                .role(Role.ADMIN)
                .gender(Gender.MALE)
                .build();

        var actualResult = createUserMapper.map(createUserDto);

        assertEquals(expectedUser, actualResult);
    }
}