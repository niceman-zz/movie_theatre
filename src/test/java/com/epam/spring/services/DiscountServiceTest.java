package com.epam.spring.services;

import com.epam.spring.config.AppConfig;
import com.epam.spring.discount.DiscountStrategy;
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
import java.util.*;
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
        LocalDateTime time = event.getEventTimetable().lastKey();
        Auditorium auditorium = event.getEventTimetable().get(time);
        Set<Integer> seatsVIP = new HashSet<>(Arrays.asList(1, 2));
        Set<Integer> seats9 = new HashSet<>(Arrays.asList(5, 7, 20, 30, 40, 50, 60, 70, 80));
        DiscountStrategy discountVIP = discountService.getDiscount(event, time, user, seatsVIP);
        DiscountStrategy discount9 = discountService.getDiscount(event, time, user, seats9);
        assertThat(discountVIP.getEffectiveDiscount(auditorium, seatsVIP), is(0));
        assertThat(discount9.getEffectiveDiscount(auditorium, seats9), is(0));
    }

    @Test
    public void shouldGetBirthdayDiscount() {
        LocalDateTime time = event.getEventTimetable().firstKey();
        Auditorium auditorium = event.getEventTimetable().get(time);
        Set<Integer> seats = Collections.singleton(1);
        DiscountStrategy discount = discountService.getDiscount(event, time, user, seats);
        assertThat(discount.getEffectiveDiscount(auditorium, seats), is(10));
    }

    @Test
    public void shouldGetPackageDiscount() {
        LocalDateTime time = event.getEventTimetable().lastKey();
        Auditorium auditorium = event.getEventTimetable().get(time);

        Set<Integer> flatSeats = new HashSet<>(Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
        DiscountStrategy flatDiscount = discountService.getDiscount(event, time, user, flatSeats);
        // 10 ordinary tickets, one is half priced, total discount is 5%
        assertThat(flatDiscount.getEffectiveDiscount(auditorium, flatSeats), is(5));

        Set<Integer> flatVipSeats = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        DiscountStrategy flatVipDiscount = discountService.getDiscount(event, time, user, flatVipSeats);
        // 10 VIP tickets, one is half priced, total discount is still 5%
        assertThat(flatVipDiscount.getEffectiveDiscount(auditorium, flatVipSeats), is(5));

        Set<Integer> mixedSeats = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11));
        DiscountStrategy discount = discountService.getDiscount(event, time, user, mixedSeats);
        // 9 VIP tickets (count as 18 ordinary) plus 1 ordinary but discount is only for 10th which is ordinary ticket.
        // So, it's as we're paying for 18.5 tickets but getting 19 (0.5 / 19 * 100 ~= 3)
        assertThat(discount.getEffectiveDiscount(auditorium, mixedSeats), is(3));

        Set<Integer> seats11 = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        DiscountStrategy discount11 = discountService.getDiscount(event, time, user, seats11);
        // 10 VIP tickets (count as 20 ordinary) plus 1 ordinary but discount is only for 10th which is VIP ticket.
        // So, it's as we're paying for 20 tickets but getting 21 (1 / 21 * 100 ~= 5)
        assertThat(discount11.getEffectiveDiscount(auditorium, seats11), is(5));

        Set<Integer> seats14 = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15));
        DiscountStrategy discount14 = discountService.getDiscount(event, time, user, seats14);
        // 9 VIP tickets (count as 18 ordinary) plus 5 ordinary but discount is only for 10th which is ordinary ticket.
        // So, it's as we're paying for 22.5 tickets but getting 23 (0.5 / 23 * 100 ~= 2)
        assertThat(discount14.getEffectiveDiscount(auditorium, seats14), is(2));

        Set<Integer> seats21 = IntStream.range(1, 22).boxed().collect(Collectors.toSet());
        DiscountStrategy discount21 = discountService.getDiscount(event, time, user, seats21);
        // 10 VIP tickets (count as 20 ordinary) plus 11 ordinary
        // but discount is only for 10th and 20th, i.e. 1 for VIP and 1 for ordinary ticket.
        // So, it's as we're paying for 29.5 tickets but getting 31 (1.5 / 31 * 100 ~= 5)
        assertThat(discount21.getEffectiveDiscount(auditorium, seats21), is(5));
    }

    @Test
    public void shouldGetComboDiscount() {
        LocalDateTime time = event.getEventTimetable().firstKey();
        Auditorium auditorium = event.getEventTimetable().get(time);
        Set<Integer> seats = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        DiscountStrategy discount = discountService.getDiscount(event, time, user, seats);
        assertThat(discount.getEffectiveDiscount(auditorium, seats), is(30));
    }
}
