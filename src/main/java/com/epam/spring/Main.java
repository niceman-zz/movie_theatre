package com.epam.spring;

import com.epam.spring.config.AppConfig;
import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.*;
import com.epam.spring.exceptions.AlreadyBookedException;
import com.epam.spring.services.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static void main(String[] args) throws AlreadyBookedException {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        List<Auditorium> auditoriums = testAuditoriumService(context);
        System.out.println("------------------------------------------------");
        testUserService(context);
        System.out.println("------------------------------------------------");
        testEventService(context, auditoriums);
        System.out.println("------------------------------------------------");
        testDiscounts(context);
        System.out.println("------------------------------------------------");
        testBooking(context);
    }

    static List<Auditorium> testAuditoriumService(ApplicationContext context) {
        AuditoriumService auditoriumService = context.getBean(AuditoriumService.class);
        List<Auditorium> auditoriums = auditoriumService.getAll();
        auditoriums.forEach(auditorium -> System.out.println(auditorium));
        return auditoriums;
    }

    static void testUserService(ApplicationContext context) {
        UserService userService = context.getBean(UserService.class);
        User kamaz = new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4));
        User ushat = new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1984, 5, 5));
        User zagon = new User("Zagon", "Baranov", "zagon@baranov.net", LocalDate.of(1985, 6, 6));
        userService.add(kamaz);
        userService.add(ushat);
        userService.add(zagon);
        System.out.println("Users");
        System.out.println("id = 1");
        System.out.println(userService.getById(1));
        System.out.println();
        System.out.println("should be Ushat");
        System.out.println(userService.getByEmail(ushat.getEmail()));
        System.out.println();
        System.out.println("All users");
        userService.getAll().forEach(u -> System.out.println(u));
        System.out.println();
        System.out.println("Removed Ushat");
        userService.remove(ushat);
        userService.getAll().forEach(u -> System.out.println(u));
    }

    static void testEventService(ApplicationContext context, List<Auditorium> auditoriums) {
        EventService eventService = context.getBean(EventService.class);
        EventCountersService eventCountersService = context.getBean(EventCountersService.class);

        Event metallica = new Event("Metallica", LocalDateTime.of(2020, 1, 20, 21, 0), auditoriums.get(0), 4000, Rating.HIGH);
        Event bi2 = new Event("Bi-2", LocalDateTime.of(2020, 3, 4, 20, 0), auditoriums.get(0), 2000.0, Rating.HIGH);
        bi2.getEventTimetable().put(LocalDateTime.of(2020, 4, 5, 20, 0), auditoriums.get(1));
        Event billie = new Event("Billie Eilish", LocalDateTime.of(2020, 4, 21, 21, 0), auditoriums.get(1), 3000.0, Rating.HIGH);
        Event loboda = new Event("Loboda", LocalDateTime.of(2020, 2, 23, 19, 0), auditoriums.get(2), 5000.0, Rating.LOW);
        Event zemfira = new Event("Zemfira", LocalDateTime.of(2020, 2, 24, 19, 30), auditoriums.get(3), 1500.0, Rating.MID);
        eventService.save(metallica);
        eventService.save(bi2);
        eventService.save(billie);
        eventService.save(loboda);
        eventService.save(zemfira);

        System.out.println("All events");
        eventService.getAll().forEach(event -> System.out.println(event));
        System.out.println();
        System.out.println("Let's check Billie (by id)");
        System.out.println(eventService.getById(billie.getId()));
        System.out.println();
        System.out.println("Now Zemfira (by name)");
        System.out.println(eventService.getByName(zemfira.getName()));
        System.out.println();
        System.out.println("What do we have in February and March?");
        eventService.getForDateRange(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 31))
                .forEach(event -> System.out.println(event));
        System.out.println();
        System.out.println("What do we have up to the end of February?");
        eventService.getNextEvents(LocalDate.of(2020, 2, 29)).forEach(event -> System.out.println(event));
        System.out.println();
        System.out.println("Loboda's concert has been cancelled");
        eventService.remove(loboda);
        eventService.getAll().forEach(event -> System.out.println(event));
        System.out.println();
        System.out.println("Let's check all events again");
        eventService.getAll().forEach(event -> System.out.println(event));
        System.out.println();
        System.out.println("Zemfira was accessed by name " + eventCountersService.getNameCounter(zemfira.getId()) + " time(s)");
        System.out.println("While Metallica only " + eventCountersService.getNameCounter(metallica.getId()) + " time(s)");
    }

    static void testDiscounts(ApplicationContext context) {
        DiscountService discountService = context.getBean(DiscountService.class);

        UserService userService = context.getBean(UserService.class);
        User kamaz = userService.getByEmail("kamaz@othodov.net");
        User zagon = userService.getByEmail("zagon@baranov.net");

        EventService eventService = context.getBean(EventService.class);
        Event bi2 = eventService.getByName("Bi-2");

        System.out.println("Discounts");
        System.out.println("Kamaz wants to buy 4 tickets to Bi-2 concert on March 4, 2020");
        Set<Integer> seats = new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4));
        LocalDateTime time = bi2.getEventTimetable().firstKey();
        Auditorium auditorium = bi2.getEventTimetable().get(time);
        System.out.println("His discount is (should be 0): " +
                discountService.getDiscount(bi2, time, kamaz, seats).getEffectiveDiscount(auditorium, seats));
        System.out.println();
        seats.addAll(Arrays.asList(5, 6, 7, 8, 9, 10));
        System.out.println("Now Zagon wants to buy 10 tickets to the same concert");
        System.out.println("His discount is (should be 4 as we have only int discounts): " +
                discountService.getDiscount(bi2, time, zagon, seats).getEffectiveDiscount(auditorium, seats));
        System.out.println();
        System.out.println("Kamaz's wife asked him to buy also 2 tickets for the concert in April");

        LocalDateTime anotherTime = bi2.getEventTimetable().lastKey();
        Auditorium anotherAuditorium = bi2.getEventTimetable().get(anotherTime);
        seats.clear();
        seats.add(1);
        seats.add(2);
        System.out.println("His discount is (should be 10 as birthday discount):" +
                discountService.getDiscount(bi2, anotherTime, kamaz, seats).getEffectiveDiscount(anotherAuditorium, seats));
        System.out.println();
        System.out.println("But then he decided to invite all his friends again");
        seats.addAll(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10));
        System.out.println("His discount is (should be 30 as combo discount): " +
                discountService.getDiscount(bi2, anotherTime, kamaz, seats).getEffectiveDiscount(anotherAuditorium, seats));
        System.out.println();

        System.out.println("Discounts applied:");
        List<DiscountStrategy> discountStrategies = discountService.getAllStrategies();
        discountStrategies.forEach(strategy -> System.out.println(strategy.getClass().getSimpleName() + ": "
                + discountService.getDiscountCounter(strategy)));
        System.out.println();

        System.out.println("Discounts by user:");
        discountStrategies.forEach(strategy -> {
            System.out.println(strategy.getClass().getSimpleName());
            System.out.println(String.format("%s: %d, %s: %d", kamaz.getFullName(),
                    discountService.getDiscountCounterByUser(strategy, kamaz), zagon.getFullName(),
                    discountService.getDiscountCounterByUser(strategy, zagon)));
        });

    }

    private static void testBooking(ApplicationContext context) throws AlreadyBookedException {
        User user = context.getBean(UserService.class).getById(1);
        EventService eventService = context.getBean(EventService.class);
        EventCountersService eventCountersService = context.getBean(EventCountersService.class);

        Event bi2 = eventService.getByName("Bi-2");
        Event zemfira = eventService.getByName("Zemfira");
        Set<Integer> seats = new HashSet<>();
        seats.add(1);

        System.out.println("Check booking");
        BookingService bookingService = context.getBean(BookingService.class);
        System.out.println("Let's check the price of 1 Bi-2 ordinary ticket (2400 considering high rating): " +
                bookingService.getTicketsPrice(bi2, bi2.getEventTimetable().firstKey(), user, seats));
        System.out.println();
        seats.clear();
        seats.add(3);
        System.out.println("Now we'll check the price of 1 VIP ticket to Bi-2 (4800): " +
                bookingService.getTicketsPrice(bi2, bi2.getEventTimetable().firstKey(), user, seats));
        System.out.println("And zemfira (3000 VIP): " +
                bookingService.getTicketsPrice(zemfira, zemfira.getEventTimetable().firstKey(), user, seats));
        System.out.println();

        seats.add(1);
        System.out.println("Now VIP and ordinary for Bi-2 (7200): " +
                bookingService.getTicketsPrice(bi2, bi2.getEventTimetable().firstKey(), user, seats));
        System.out.println();

        seats.addAll(Arrays.asList(2, 4, 5, 6, 7, 8, 9, 10));
        System.out.println("2 VIP and 8 ordinary tickets with discount for 10 tickets package (27648): " +
                bookingService.getTicketsPrice(bi2, bi2.getEventTimetable().firstKey(), user, seats));
        System.out.println();

        System.out.println("Let's try to book some tickets");
        Set<Ticket> tickets = new HashSet<>();
        tickets.add(new Ticket(bi2, 1, bi2.getEventTimetable().firstKey(), user));
        bookingService.bookTickets(tickets);
        System.out.println("Should be 1 ticket booked for the nearest Bi-2 concert (March 4): " +
                bookingService.getPurchasedTicketsForEvent(bi2, bi2.getEventTimetable().firstKey()));
        System.out.println(String.format("%s has ticket to Bi-2 concert: %s", user.getFullName(), user.getTickets()));
        System.out.println();

        tickets.clear();
        tickets.add(new Ticket(bi2, 2, bi2.getEventTimetable().lastKey(), user));
        tickets.add(new Ticket(bi2, 3, bi2.getEventTimetable().lastKey(), user));
        bookingService.bookTickets(tickets);
        System.out.println("Booked 2 tickets for the April's concert of Bi-2: " +
                bookingService.getPurchasedTicketsForEvent(bi2, bi2.getEventTimetable().lastKey()));
        System.out.println();

        System.out.println("Let's check that we still have previously booked ticket: " +
                bookingService.getPurchasedTicketsForEvent(bi2, bi2.getEventTimetable().firstKey()));
        System.out.println();
        System.out.println("Prices to Bi-2 concerts were checked " + eventCountersService.getPriceCheckCounter(bi2.getId())
                + " time(s)");
        System.out.println("Zemfira - " + eventCountersService.getPriceCheckCounter(zemfira.getId()) + " time(s)");
        System.out.println();
        System.out.println("Tickets to Bi-2 concerts were booked " + eventCountersService.getBookCounter(bi2.getId())
                + " time(s)");
        System.out.println("Zemfira - " + eventCountersService.getBookCounter(zemfira.getId()) + " time(s)");
    }
}
