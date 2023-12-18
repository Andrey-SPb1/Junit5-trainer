package com.dmdev.dao;

import com.dmdev.entity.Gender;
import com.dmdev.entity.Role;
import com.dmdev.entity.User;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.util.ConnectionManager;
import com.dmdev.util.LocalDateFormatter;
import lombok.SneakyThrows;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoIT extends IntegrationTestBase {

    private final UserDao userDao = UserDao.getInstance();

    private static final User IVAN = new User(
            1, "Ivan", LocalDate.of(1990, 1, 10),
            "ivan@gmail.com", "111", Role.ADMIN, Gender.MALE
    );

    @Nested
    @DisplayName("Test find all functionality")
    @Tag("find_all")
    class FindAllTest {

        @Test
        void failIfListIsEmpty() {
            List<User> users = userDao.findAll();

            assertFalse(users.isEmpty());
        }

        @Test
        @SneakyThrows
        void throwsExceptionIfDatabaseIsEmpty() {
            try (var connection = ConnectionManager.get();
                 var statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS users;");
            }
            assertThrows(JdbcSQLSyntaxErrorException.class, userDao::findAll);
        }
    }

    @Nested
    @DisplayName("Test find by id functionality")
    @Tag("find_by_id")
    class FindByIdTest {

        @Test
        void usersIsEmptyIfIdIsNotCorrect() {
            var optionalUser = userDao.findById(0);
            var optionalUser1 = userDao.findById(null);

            assertAll(
                    () -> assertTrue(optionalUser.isEmpty()),
                    () -> assertTrue(optionalUser1.isEmpty())
            );

        }

        @Test
        void findByIdTest() {
            var user = userDao.findById(IVAN.getId()).isPresent() ?
                    userDao.findById(IVAN.getId()).get() : null;

            if (user != null) {
                assertAll(
                        () -> assertEquals(IVAN.getName(), user.getName()),
                        () -> assertEquals(IVAN.getBirthday(), user.getBirthday()),
                        () -> assertEquals(IVAN.getEmail(), user.getEmail()),
                        () -> assertEquals(IVAN.getPassword(), user.getPassword()),
                        () -> assertEquals(IVAN.getRole(), user.getRole()),
                        () -> assertEquals(IVAN.getGender(), user.getGender())
                );
            } else {
                fail();
            }
        }
    }

    @Nested
    @DisplayName("Test save user functionality")
    @Tag("save")
    class SaveTest {

        @Test
        void throwsExceptionIfUserIsNullOrEmpty() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> userDao.save(null)),
                    () -> assertThrows(JdbcSQLIntegrityConstraintViolationException.class,
                            () -> userDao.save(new User()))
            );
        }

        @Test
        @SneakyThrows
        void checkGeneratedKeys() {

            String SQLDescOrder = "SELECT id FROM users ORDER BY id DESC LIMIT 1";
            Integer userId = null;
            User user = User.builder()
                    .name("Dmitriy")
                    .birthday(LocalDateFormatter.format("1993-09-22"))
                    .email("dmitriy@gmail.com")
                    .password("531")
                    .role(Role.USER)
                    .gender(Gender.MALE)
                    .build();

            User userWithId = userDao.save(user);

            try (var connection = ConnectionManager.get();
                 var preparedStatement = connection.prepareStatement(SQLDescOrder)) {

                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getObject("id", Integer.class);
                }
            }

            assertEquals(userWithId.getId(), userId);

        }
    }

    @Nested
    @DisplayName("Test find by email and password functionality")
    @Tag("find_by_email_and_password")
    class FindByEmailAndPasswordTest {

        //        @ParameterizedTest(name = "{arguments} test")
        @ParameterizedTest
        @MethodSource("getArgumentsForTest")
        void checkParamForMethod(String email, String password, Optional<User> expectedUser) {
            var actualUser = userDao.findByEmailAndPassword(email, password);

            assertEquals(expectedUser, actualUser);
        }

        @DisplayName("find by email and password parameters")
        static Stream<Arguments> getArgumentsForTest() {
            return Stream.of(
                    Arguments.of("test", "111", Optional.empty()),
                    Arguments.of("ivan@gmail.com", "test", Optional.empty()),
                    Arguments.of(null, "111", Optional.empty()),
                    Arguments.of("ivan@gmail.com", null, Optional.empty()),
                    Arguments.of("ivan@gmail.com", "111", Optional.of(IVAN))
            );
        }

    }

    @Nested
    @DisplayName("Test delete method functionality")
    @Tag("delete_test")
    class DeleteTest {

        @Test
        void checkMethodIfParamIsNotCorrectAndCorrect() {
            assertAll(
                    () -> assertFalse(userDao.delete(0)),
                    () -> assertFalse(userDao.delete(-1)),
                    () -> assertFalse(userDao.delete(8)),
                    () -> assertTrue(userDao.delete(1))
            );
        }

        @Test
        @SneakyThrows
        void deleteUserTest() {
            Integer count = null;
            var deleted = userDao.delete(1);

            if (deleted) {
                try (var connection = ConnectionManager.get();
                     var preparedStatement = connection.prepareStatement(" SELECT count(*) AS users_count FROM users")) {

                    var resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        count = resultSet.getObject("users_count", Integer.class);
                    }
                }
            }
            assertEquals(4, count);
        }
    }

    @Nested
    @DisplayName("Test update user functionality")
    @Tag("update")
    class UpdateTest {

        @Test
        void throwsExceptionIfUserIsNullOrEmpty() {
            var user = new User();
            user.setId(1);

            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> userDao.update(null)),
                    () -> assertThrows(JdbcSQLIntegrityConstraintViolationException.class, () -> userDao.update(user))
            );
        }

        @Test
        @SneakyThrows
        void updateUserTest() {
            var yana = new User(1, "Yana", LocalDateFormatter.format("1999-12-09"),
                    "yana@gmail.com", "923", Role.USER, Gender.FEMALE);
            userDao.update(yana);

            var optionalUser = userDao.findById(yana.getId());

            if(optionalUser.isPresent()) {
                var user = optionalUser.get();

                assertAll(
                        () -> assertEquals(yana.getName(), user.getName()),
                        () -> assertEquals(yana.getBirthday(), user.getBirthday()),
                        () -> assertEquals(yana.getEmail(), user.getEmail()),
                        () -> assertEquals(yana.getPassword(), user.getPassword()),
                        () -> assertEquals(yana.getRole(), user.getRole()),
                        () -> assertEquals(yana.getGender(), user.getGender())
                );
            } else {
                fail();
            }
        }
    }
}
