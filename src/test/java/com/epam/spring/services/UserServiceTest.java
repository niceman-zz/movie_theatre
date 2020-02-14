package com.epam.spring.services;

import com.epam.spring.config.AppConfig;
import com.epam.spring.domain.User;
import com.epam.spring.exceptions.MovieTheatreException;
import org.junit.jupiter.api.AfterEach;
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
@ContextConfiguration(classes = {AppConfig.class})
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @AfterEach
    public void refresh() {
        userService.clear();
    }

    @Test
    public void shouldCreateUser() {
        User user = userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        assertThat(user.getId(), notNullValue());
        assertThat(user.getEmail(), is("kamaz@othodov.net"));
        assertThat(user.getFirstName(), is("Kamaz"));
        assertThat(user.getLastName(), is("Othodov"));
        assertThat(user.getBirthday(), equalTo(LocalDate.of(1983, 4, 4)));
    }

    @Test
    public void shouldNotCreateAnotherUserWithSameEmail() {
        userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        assertThrows(MovieTheatreException.class,
                () -> userService.add(new User("Ushat", "Pomoev", "kamaz@othodov.net", LocalDate.of(1988, 8, 8))));
    }

    @Test
    public void shouldGetUserById() {
        User user = userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User user2 = userService.getById(user.getId());
        assertThat(user2, is(user));
    }

    @Test
    public void shouldGetUserByEmail() {
        User user = userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User user2 = userService.getByEmail("kamaz@othodov.net");
        assertThat(user2, is(user));
    }

    @Test
    public void shouldReturnAllUsers() {
        userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        userService.add(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        assertThat(userService.getAll().size(), equalTo(2));
    }

    @Test
    public void shouldRemoveUser() {
        User toKeep = userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        User toRemove = userService.add(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        userService.remove(toRemove);
        assertThat(userService.getAll().size(), equalTo(1));
        assertThat(userService.getAll().get(0), is(toKeep));
    }

    @Test
    public void shouldNotRemoveAnyUserIfProvidedUserDoesNotExist() {
        userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4)));
        userService.add(new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8)));
        User toRemove = new User("Zagon", "Baranov", "zagon@baranov.net", LocalDate.of(1990, 2, 2));

        assertThat(userService.remove(toRemove), is(false));
        assertThat(userService.getAll().size(), equalTo(2));
    }

    @Test
    public void shouldUpdateUser() {
        User user = new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4));
        userService.add(user);
        User copy = new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthday());
        copy.setId(user.getId());
        copy.setEmail("1@2.net");

        userService.update(copy);
        assertThat(user, not(copy));
        assertThat(userService.getById(user.getId()), equalTo(copy));
    }

    @Test
    public void shouldNotUpdateWhenUserDoesNotExist() {
        User user = new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4));
        userService.add(user);
        User another = new User("I", "Don't", "exist", LocalDate.of(1, 1, 1));
        assertThrows(MovieTheatreException.class, () -> userService.update(another));

        another.setId(-1L);
        assertThrows(MovieTheatreException.class, () -> userService.update(another));
    }

    @Test
    public void checkRegisteredUser() {
        User registered = new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4));
        userService.add(registered);
        assertThat(userService.isRegistered(registered), is(true));
    }

    @Test
    public void checkUnregisteredUser() {
        userService.add(new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4))); // registered one

        User unregistered = new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1988, 8, 8));
        assertThat(userService.isRegistered(unregistered), is(false));

        unregistered.setId(123L);
        assertThat(userService.isRegistered(unregistered), is(false));
    }
}
