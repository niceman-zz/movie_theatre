package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.User;
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
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:spring-test.xml")
public class DiscountServiceTest {
    @Autowired
    private DiscountService discountService;

    private static Event event;
    private static User user;

    @BeforeAll
    public static void init() {
        Auditorium auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
        TreeMap<LocalDateTime, Auditorium> timetable = new TreeMap<>();
        timetable.put(LocalDateTime.of(2020, 5, 5, 12, 0), auditorium);
        timetable.put(LocalDateTime.of(2020, 4, 5, 12, 0), auditorium);
        event = new Event("some event", timetable, 200, Rating.MID);
        user = new User("Ivan", "Ivanich", "ivan@ivanich.ru", LocalDate.of(1988, 4, 4));
    }

    @Test
    public void shouldNotGetAnyDiscount() {
        int discountVIP = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2)));
        int discount9 = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(5, 7, 20, 30, 40, 50, 60, 70, 80)));
        assertThat(discountVIP, is(0));
        assertThat(discount9, is(0));
    }

    @Test
    public void shouldGetBirthdayDiscount() {
        int discount = discountService.getDiscount(event, event.getEventTimetable().firstKey(), user,
                Collections.singleton(1));
        assertThat(discount, is(10));
    }

    @Test
    public void shouldGetPackageDiscount() {
        int discount = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(discount, is(5));
        int incorrectYetValidDiscount = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)));
        assertThat(incorrectYetValidDiscount, is(5));
    }

    @Test
    public void shouldGetComboDiscount() {
        int discount = discountService.getDiscount(event, event.getEventTimetable().firstKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(discount, is(30));
    }
}
