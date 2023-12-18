package com.dmdev.service;

import com.dmdev.dao.UserDao;
import com.dmdev.dto.CreateUserDto;
import com.dmdev.entity.Gender;
import com.dmdev.entity.Role;
import com.dmdev.entity.User;
import com.dmdev.exception.ValidationException;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateUserMapper;
import com.dmdev.mapper.UserMapper;
import com.dmdev.util.LocalDateFormatter;
import com.dmdev.validator.CreateUserValidator;
import com.dmdev.validator.Error;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Use verify (34th min)
 */

public class UserServiceIT extends IntegrationTestBase {

    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void init() {
        userDao = UserDao.getInstance();
        userService = new UserService(
                CreateUserValidator.getInstance(),
                userDao,
                CreateUserMapper.getInstance(),
                UserMapper.getInstance()
        );
    }
    @Nested
    @DisplayName("Test user login functionality")
    @Tag("login")
    class LoginTest {

        private final User IVAN = new User(
                1, "Ivan", LocalDate.of(1990, 1, 10),
                "ivan@gmail.com", "111", Role.ADMIN, Gender.MALE
        );
        @Test
        void login() {
            var userDto = userService.login(IVAN.getEmail(), IVAN.getPassword());

            assertAll(
                    () -> assertThat(userDto.isPresent()).isTrue(),
                    () -> userDto.ifPresent(user -> assertThat(user.getName()).isEqualTo(IVAN.getName()))
            );
        }

        @Test
        void loginFailIfPasswordOrEmailIsNotCorrect() {

            var userDtoWithTestPassword = userService.login(IVAN.getEmail(), "dummy");
            var userDtoWithTestEmail = userService.login("dummy", IVAN.getPassword());

            assertAll(
                    () -> assertThat(userDtoWithTestEmail).isEmpty(),
                    () -> assertThat(userDtoWithTestPassword).isEmpty()
            );

        }

        @Test
        void loginFailIfPasswordOrEmailIsNull() {

            var userDtoWithTestPassword = userService.login(IVAN.getEmail(), null);
            var userDtoWithTestEmail = userService.login(null, IVAN.getPassword());

            assertAll(
                    () -> assertThat(userDtoWithTestEmail).isEmpty(),
                    () -> assertThat(userDtoWithTestPassword).isEmpty()
            );

        }
    }

    @Nested
    @DisplayName("Test user create functionality")
    @Tag("create")
    class CreateTest {

        private final CreateUserDto failUser = CreateUserDto.builder()
                .birthday("2007.01.28")
                .role(null)
                .gender(null)
                .build();
        private final  CreateUserDto MAX = CreateUserDto.builder()
                .name("Maksim")
                .birthday("2007-01-28")
                .email("maksim@gmail.com")
                .password("321")
                .role("USER")
                .gender("MALE")
                .build();

        private final CreateUserDto user = CreateUserDto.builder()
                .birthday("2007-01-28")
                .role("USER")
                .gender("MALE")
                .build();
        @Test
        void createTest() {
            var userDto = userService.create(MAX);

            Role.find(MAX.getRole());

            assertAll(
                    () -> assertEquals(MAX.getName(), userDto.getName()),
                    () -> assertTrue(LocalDateFormatter.format(MAX.getBirthday()).isEqual(userDto.getBirthday())),
                    () -> assertEquals(MAX.getEmail(), userDto.getEmail()),
                    () -> assertThat(Role.find(MAX.getRole()).isPresent() ?
                            Role.find(MAX.getRole()).get() : null).isEqualTo(userDto.getRole()),
                    () -> assertThat(Gender.find(MAX.getGender()).isPresent() ?
                            Gender.find(MAX.getGender()).get() : null).isEqualTo(userDto.getGender())
            );
        }

        @Test
        void throwsExceptionIfUserIsNull() {
            assertThrows(NullPointerException.class, () -> userService.create(null));
        }

        @Test
        void throwsExceptionIfDataIsNotCorrectOrNull() {
            assertThrows(ValidationException.class, () -> userService.create(failUser));
        }

        @Test
        void validationExceptionTest() {
            List<Error> errors = new ArrayList<>();

            try {
                userService.create(failUser);
            } catch (ValidationException validationException) {
                errors = validationException.getErrors();
            }
            
            assertThat(errors).isNotEmpty();
        }

    }
}
