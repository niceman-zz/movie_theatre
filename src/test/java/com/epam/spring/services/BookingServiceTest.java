package com.epam.spring.services;

import com.epam.spring.config.AppConfig;
import com.epam.spring.domain.*;
import com.epam.spring.exceptions.AlreadyBookedException;
import com.epam.spring.exceptions.MovieTheatreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class BookingServiceTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    private static Event event;
    private static Event highRankedEvent;
    private static User user;

    @BeforeEach
    public void bootstrap() {
        Auditorium auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
        TreeMap<LocalDateTime, Auditorium> timetable = new TreeMap<>();
        timetable.put(LocalDateTime.of(2020, 5, 5, 12, 0), auditorium);
        timetable.put(LocalDateTime.of(2020, 4, 22, 12, 0), auditorium); // never booked
        timetable.put(LocalDateTime.of(2020, 4, 5, 12, 0), auditorium);

        event = eventService.save(new Event("some event", timetable, 200, Rating.MID));
        highRankedEvent = eventService.save(new Event("Event with huge ratings", timetable, 1000, Rating.HIGH));
        user = userService.add(new User("Ivan", "Ivanich", "ivan@ivanich.ru", LocalDate.of(1988, 4, 4)));
    }

    @AfterEach
    public void clear() {
        userService.clear();
        eventService.clear();
        bookingService.clear();
    }

    @Test
    public void checkTicketPriceWithoutDiscountOrCharges() {
        double price = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user,
                Collections.singleton(10));
        assertThat(price, is(200.0));

        double vipPrice = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user,
                Collections.singleton(1));
        assertThat(vipPrice, is(400.0));
    }

    @Test
    public void shouldReturnZeroWhenSeatsWereNotPassed() {
        double price = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user, null);
        assertThat(price, is(0.0));

        price = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user, new HashSet<>());
        assertThat(price, is(0.0));
    }

    @Test
    public void ticketsToEventsWithHighRatingsShouldCostMoreThanEventsBasePrice() {
        double price = bookingService.getTicketsPrice(highRankedEvent, highRankedEvent.getEventTimetable().lastKey(),
                user, Collections.singleton(10));
        assertThat(price, greaterThan(highRankedEvent.getPrice()));
        assertThat(price, is(1200.0));

        double vipPrice = bookingService.getTicketsPrice(highRankedEvent, highRankedEvent.getEventTimetable().lastKey(),
                user, Collections.singleton(1));
        assertThat(vipPrice, is(2400.0));
    }

    @Test
    public void testMultiTicketsPrice() {
        double price = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(10, 11, 12, 13, 14, 15)));
        assertThat(price, is(1200.0));

        double vipPrice = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        assertThat(vipPrice, is(2000.0));

        double mixedPrice = bookingService.getTicketsPrice(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 13, 14, 15)));
        assertThat(mixedPrice, is(1400.0));
    }

    @Test
    public void checkBirthday10PercentDiscount() {
        double price = bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user,
                Collections.singleton(10));
        assertThat(price, is(180.0));

        double vipPrice = bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user,
                Collections.singleton(1));
        assertThat(vipPrice, is(360.0));

        double mixedPrice = bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user,
                new HashSet<>(Arrays.asList(1, 5, 10, 15)));  // (400 x 2 + 200 x 2) - 10%
        assertThat(mixedPrice, is(1080.0));

        double highRankedPrice = bookingService.getTicketsPrice(highRankedEvent,
                highRankedEvent.getEventTimetable().firstKey(), user, new HashSet<>(Arrays.asList(1, 5, 10, 15)));
        assertThat(highRankedPrice, is(6480.0));  // (2000 x 2 + 1000 x 2) * 1.2 - 10%
    }

    @Test
    public void shouldNotReturnTicketsForUnregisteredEvent() {
        Event unregisteredEvent = new Event("unregistered", event.getEventTimetable().firstKey(),
                event.getEventTimetable().values().iterator().next(), 1, Rating.MID);
        List<Ticket> tickets = bookingService.getPurchasedTicketsForEvent(
                unregisteredEvent, unregisteredEvent.getEventTimetable().firstKey());
        assertThat(tickets, empty());
    }

    @Test
    public void shouldNotReturnTicketsWhenNotBookedOnParticularTime() throws AlreadyBookedException {
        Ticket tkt1 = new Ticket(event, 9, event.getEventTimetable().firstKey(), user);
        Ticket tkt2 = new Ticket(event, 19, event.getEventTimetable().lastKey(), user);
        bookingService.bookTickets(new HashSet<>(Arrays.asList(tkt1, tkt2)));

        LocalDateTime nonexistentTime = LocalDateTime.of(2020, 1, 1, 1, 1);
        List<Ticket> tickets = bookingService.getPurchasedTicketsForEvent(event, nonexistentTime);
        assertThat(tickets, empty());

        Iterator<LocalDateTime> it = event.getEventTimetable().keySet().iterator();
        it.next(); // skip first
        LocalDateTime neverBookedTime = it.next(); // second key is never booked
        tickets = bookingService.getPurchasedTicketsForEvent(event, neverBookedTime);
        assertThat(tickets, empty());
    }

    @Test
    public void shouldReturnBookedTicketsForEvent() throws AlreadyBookedException {
        Ticket tkt1 = new Ticket(event, 1, event.getEventTimetable().firstKey(), user);
        bookingService.bookTickets(Collections.singleton(tkt1));
        List<Ticket> bookedTicket =
                bookingService.getPurchasedTicketsForEvent(event, event.getEventTimetable().firstKey());
        assertThat(bookedTicket.get(0), equalTo(tkt1));

        Ticket tkt2 = new Ticket(event, 10, event.getEventTimetable().firstKey(), user);
        bookingService.bookTickets(Collections.singleton(tkt2));
        List<Ticket> bookedTickets =
                bookingService.getPurchasedTicketsForEvent(event, event.getEventTimetable().firstKey());
        assertThat(bookedTickets.size(), is(2));
        assertThat(bookedTickets.get(0), not(bookedTickets.get(1)));
    }

    @Test
    public void usersShouldHaveTicketAfterTheyBookedIt() throws AlreadyBookedException {
        Ticket ticket = new Ticket(event, 1, event.getEventTimetable().firstKey(), user);

        bookingService.bookTickets(Collections.singleton(ticket));
        assertThat(user.getTickets(), not(empty()));
        assertThat(user.getTickets().iterator().next(), is(ticket));
        assertThat(user.getTickets().equals(user.getTickets()), is(true));
        userService.clear();
    }

    @Test
    public void shouldNotAllowBookAlreadyBookedTickets() throws AlreadyBookedException {
        LocalDateTime time = event.getEventTimetable().firstKey();
        Ticket tkt1 = new Ticket(event, 1, time, user);
        Ticket tkt2 = new Ticket(event, 2, time, user);
        bookingService.bookTickets(new HashSet<>(Arrays.asList(tkt1, tkt2)));

        Ticket tkt3 = new Ticket(event, 2, time, user);
        Ticket tkt4 = new Ticket(event, 3, time, user);

        AlreadyBookedException exception = assertThrows(AlreadyBookedException.class,
                () -> bookingService.bookTickets(new HashSet<>(Arrays.asList(tkt3, tkt4))));
        assertThat(exception.getMessage().contains("[2]"), is(true));
    }
}
