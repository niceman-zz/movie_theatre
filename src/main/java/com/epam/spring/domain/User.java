package com.epam.spring.domain;

import java.time.LocalDate;
import java.util.*;

public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthday;
    private Set<Ticket> tickets;
    private List<String> luckyWinnerMessages;
    private boolean lucky;

    public User() {
        tickets = new HashSet<>();
        luckyWinnerMessages = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, LocalDate birthday) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthday = birthday;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + ' ' + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(birthday, user.birthday) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, birthday, email);
    }

    public boolean isLucky() {
        return lucky;
    }

    public void setLucky(boolean lucky) {
        this.lucky = lucky;
    }

    public List<String> getLuckyWinnerMessages() {
        return luckyWinnerMessages;
    }
}
