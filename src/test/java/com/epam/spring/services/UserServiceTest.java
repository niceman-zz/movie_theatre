package com.epam.spring.services;

import com.epam.spring.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:spring-test.xml")
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @BeforeEach
    public void refresh() {
        userService.clear();
    }

    @Test
    public void shouldCreateUser() {
        User user = userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        assertThat(user.getId(), notNullValue());
        assertThat(user.getEmail(), is("kamaz@othodov.net"));
        assertThat(user.getFirstName(), is("Kamaz"));
        assertThat(user.getLastName(), is("Othodov"));
        assertThat(user.getBirthday(), equalTo(LocalDate.of(1983, 4, 4)));
    }

    @Test
    public void shouldNotCreateAnotherUserWithSameEmail() {
        userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        assertThrows(IllegalArgumentException.class,
                () -> userService.save(new User("Ushat", "Pomoev", "kamaz@othodov.net", LocalDate.of(1988, 8, 8))));
    }

    @Test
    public void shouldGetUserById() {
        User user = userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User user2 = userService.getById(user.getId());
        assertThat(user2, is(user));
    }

    @Test
    public void shouldGetUserByEmail() {
        User user = userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User user2 = userService.getByEmail("kamaz@othodov.net");
        assertThat(user2, is(user));
    }

    @Test
    public void shouldReturnAllUsers() {
        userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        userService.save(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        assertThat(userService.getAll().size(), equalTo(2));
    }

    @Test
    public void shouldRemoveUser() {
        User toKeep = userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User toRemove = userService.save(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        userService.remove(toRemove);
        assertThat(userService.getAll().size(), equalTo(1));
        assertThat(userService.getAll().get(0), is(toKeep));
    }

    @Test
    public void shouldNotRemoveAnyUserIfProvidedUserDoesNotExist() {
        userService.save(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        userService.save(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        User toRemove = new User("Zagon", "Baranov", "zagon@baranov.net", LocalDate.of(1990, 2, 2));

        assertThat(userService.remove(toRemove), is(false));
        assertThat(userService.getAll().size(), equalTo(2));
    }
}
