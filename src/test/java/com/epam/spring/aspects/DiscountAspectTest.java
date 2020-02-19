package com.epam.spring.aspects;

import com.epam.spring.config.AppConfig;
import com.epam.spring.discount.BirthdayDiscountStrategy;
import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.User;
import com.epam.spring.services.BookingService;
import com.epam.spring.services.DiscountService;
import com.epam.spring.services.UserService;
import org.junit.jupiter.api.AfterEach;
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
public class DiscountAspectTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private UserService userService;

    @AfterEach
    private void clean() {
        userService.clear();
        bookingService.clear();
    }

    @Test
    public void shouldNotIncrementAnyDiscountCounter() {
        Set<Integer> seats = Collections.singleton(1);
        User user = userService.add(new User("Putin", "Schmutin", "putin@schmutin.ru", LocalDate.of(1952, 10, 7)));
        Auditorium auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));

        Event event = new Event("Concert", LocalDateTime.of(2020, 5, 5, 5, 5), auditorium, 1000.0, Rating.MID);
        event.setId(1L);
        Event event2 = new Event("Circus", LocalDateTime.of(2020, 6, 6, 6, 6), auditorium, 1000.0, Rating.MID);
        event2.setId(2L);
        bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), user, seats);
        bookingService.getTicketsPrice(event2, event2.getEventTimetable().firstKey(), user, seats);
        discountService.getAllStrategies().forEach(
                strategy -> assertThat(discountService.getDiscountCounter(strategy), is(0)));
    }

    @Test
    public void shouldIncrementBirthdayStrategyCounterForPutin() {
        Set<Integer> seats = Collections.singleton(1);
        User putin = userService.add(new User("Putin", "Schmutin", "putin@schmutin.ru", LocalDate.of(1952, 10, 7)));
        User medveded = userService.add(new User("Preved", "Medved", "preved@medved.ru", LocalDate.of(1965, 9, 14)));
        Auditorium auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));

        Event event = new Event("Concert", LocalDateTime.of(2020, 10, 5, 5, 5), auditorium, 1000.0, Rating.MID);
        event.setId(1L);
        Event event2 = new Event("Circus", LocalDateTime.of(2020, 6, 6, 6, 6), auditorium, 1000.0, Rating.MID);
        event2.setId(2L);
        bookingService.getTicketsPrice(event, event.getEventTimetable().firstKey(), putin, seats);
        bookingService.getTicketsPrice(event2, event2.getEventTimetable().firstKey(), medveded, seats);

        discountService.getAllStrategies().forEach(strategy -> {
            if (strategy instanceof BirthdayDiscountStrategy) {
                assertThat(discountService.getDiscountCounter(strategy), is(1));
                assertThat(discountService.getDiscountCounterByUser(strategy, putin), is(1));
                assertThat(discountService.getDiscountCounterByUser(strategy, medveded), is(0));
            } else {
                assertThat(discountService.getDiscountCounter(strategy), is(0));
            }
        });
    }
}
