package com.epam.spring.aspects;

import com.epam.spring.config.AppConfig;
import com.epam.spring.domain.*;
import com.epam.spring.services.AuditoriumService;
import com.epam.spring.services.BookingService;
import com.epam.spring.services.EventService;
import com.epam.spring.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class CounterAspectTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditoriumService auditoriumService;

    private static User user;
    private static Auditorium auditorium;

    @BeforeAll
    public static void init() {
        user = new User("Putin", "Schmutin", "putin@schmutin.ru", LocalDate.of(1952, 10, 7));
        auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @AfterEach
    public void clean() {
        eventService.clear();
    }

    @Test
    public void testGetByNameCounter() {
        Event counted = eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        Event notCounted = eventService.save(new Event("Circus", null, 1000.0, Rating.MID));
        Event byName = eventService.getByName("Concert");
        assertThat(byName, is(counted));
        assertThat(eventService.getEventCount(counted.getName()), is(1));
        assertThat(eventService.getEventCount(notCounted.getName()), is(0));
    }

    @Test
    public void testPriceChecksCounter() {
        Set<Integer> seats = Collections.singleton(1);

        Event event = eventService.save(new Event("Concert", LocalDateTime.now(), auditorium, 1000.0, Rating.MID));
        Event event2 = eventService.save(new Event("Circus", LocalDateTime.now().plusDays(1), auditorium, 1000.0, Rating.MID));
        bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user, seats);
        bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user, seats);
        assertThat(eventService.getEventChecksCount(event.getName()), is(2));
        assertThat(eventService.getEventChecksCount(event2.getName()), is(0));

        bookingService.getTicketsPrice(event2, event2.getEventTimetable().firstKey(), user, seats);
        assertThat(eventService.getEventChecksCount(event.getName()), is(2));
        assertThat(eventService.getEventChecksCount(event2.getName()), is(1));
    }

    @Test
    public void testBookingsCounter() {
        Event event = eventService.save(new Event("Concert", LocalDateTime.now(), auditorium, 1000.0, Rating.MID));
        Event event2 = eventService.save(new Event("Circus", LocalDateTime.now().plusDays(1), auditorium, 1000.0, Rating.MID));
        Ticket ticket = new Ticket(event, 1, event.getEventTimetable().firstKey(), user);
        Ticket ticket2 = new Ticket(event, 2, event.getEventTimetable().firstKey(), user);
        Ticket ticket3 = new Ticket(event2, 3, event2.getEventTimetable().firstKey(), user);

        bookingService.bookTickets(Collections.singleton(ticket));
        bookingService.bookTickets(Collections.singleton(ticket2));
        bookingService.bookTickets(Collections.singleton(ticket3));
        assertThat(eventService.getEventBookings(event.getName()), is(2));
        assertThat(eventService.getEventBookings(event2.getName()), is(1));
    }
}
