package com.epam.spring.services;

import com.epam.spring.config.AppConfig;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class DiscountServiceTest {
    @Autowired
    private DiscountService discountService;

    private static Event event;
    private static User user;

    @BeforeAll
    public static void init() {
        Auditorium auditorium = new Auditorium("Big hall", 300, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
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
        int flatDiscount = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)));
        assertThat(flatDiscount, is(5)); // 10 ordinary tickets, one is half priced, total discount is 5%

        int flatVipDiscount = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(flatVipDiscount, is(5)); // 10 VIP tickets, one is half priced, total discount is still 5%

        int discount = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11)));
        assertThat(discount, is(3));  // 9 VIP tickets (count as 18 ordinary) plus 1 ordinary but discount is only
                                      // for 10th which is ordinary ticket.
                                      // So, it's as we're paying for 18.5 tickets but getting 19 (0.5 / 19 * 100 ~= 3)

        int discount11 = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));
        assertThat(discount11, is(5));  // 10 VIP tickets (count as 20 ordinary) plus 1 ordinary
                                        // but discount is only for 10th which is VIP ticket.
                                        // So, it's as we're paying for 20 tickets but getting 21 (1 / 21 * 100 ~= 5)

        int discount14 = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15)));
        assertThat(discount14, is(2));  // 9 VIP tickets (count as 18 ordinary) plus 5 ordinary
                                        // but discount is only for 10th which is ordinary ticket.
                                        // So, it's as we're paying for 22.5 tickets but getting 23 (0.5 / 23 * 100 ~= 2)

        int discount21 = discountService.getDiscount(event, event.getEventTimetable().lastKey(), user,
                IntStream.range(1, 22).boxed().collect(Collectors.toSet()));
        assertThat(discount21, is(5));  // 10 VIP tickets (count as 20 ordinary) plus 11 ordinary
                                        // but discount is only for 10th and 20th, i.e. 1 for VIP and 1 for ordinary ticket.
                                        // So, it's as we're paying for 29.5 tickets but getting 31 (1.5 / 31 * 100 ~= 5)
    }

    @Test
    public void shouldGetComboDiscount() {
        int discount = discountService.getDiscount(event, event.getEventTimetable().firstKey(), user,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(discount, is(30));
    }
}
