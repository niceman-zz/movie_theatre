package com.epam.spring;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.User;
import com.epam.spring.services.AuditoriumService;
import com.epam.spring.services.EventService;
import com.epam.spring.services.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        AuditoriumService auditoriumService = context.getBean(AuditoriumService.class);
        List<Auditorium> auditoriums = auditoriumService.getAll();
        auditoriums.forEach(auditorium -> System.out.println(auditorium));
        System.out.println("------------------------------------------------");
        List<User> users = testUserService(context);
        System.out.println("------------------------------------------------");
        List<Event> events = testEventService(context, auditoriums);
    }

    static List<User> testUserService(ApplicationContext context) {
        UserService userService = context.getBean(UserService.class);
        User user = new User("Kamaz", "Othodov", "kamaz@othodov.net", LocalDate.of(1983, 4, 4));
        User user2 = new User("Ushat", "Pomoev", "ushat@pomoev.net", LocalDate.of(1984, 5, 5));
        User user3 = new User("Zagon", "Baranov", "zagon@baranov.net", LocalDate.of(1985, 6, 6));
        userService.save(user);
        userService.save(user2);
        userService.save(user3);
        System.out.println("Users");
        System.out.println("id = 1");
        System.out.println(userService.getById(1));
        System.out.println();
        System.out.println("should be Ushat");
        System.out.println(userService.getByEmail(user2.getEmail()));
        System.out.println();
        System.out.println("All users");
        userService.getAll().forEach(u -> System.out.println(u));
        System.out.println();
        System.out.println("Removed Ushat");
        userService.remove(user2);
        userService.getAll().forEach(u -> System.out.println(u));

        return userService.getAll();
    }

    static List<Event> testEventService(ApplicationContext context, List<Auditorium> auditoriums) {
        EventService eventService = context.getBean(EventService.class);

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

        return eventService.getAll();
    }
}
