package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:spring-test.xml")
public class EventServiceTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private AuditoriumService auditoriumService;

    @BeforeEach
    public void refresh() {
        eventService.clear();
    }

    @Test
    public void shouldCreateEvent() {
        Event event = eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        assertThat(event.getId(), notNullValue());
        assertThat(event.getName(), is("Concert"));
        assertThat(event.getPrice(), equalTo(1000.0));
        assertThat(event.getRating(), is(Rating.MID));
    }

    @Test
    public void shouldNotCreateEventWithSameName() {
        eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        assertThrows(IllegalArgumentException.class,
                () -> eventService.save(new Event("Concert", null, 2000.0, Rating.LOW)));

    }

    @Test
    public void shouldGetEventById() {
        Event event = eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        Event event2 = eventService.getById(event.getId());
        assertThat(event2, is(event));
    }

    @Test
    public void shouldGetEventByName() {
        Event event = eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        Event event2 = eventService.getByName("Concert");
        assertThat(event2, is(event));
    }

    @Test
    public void shouldReturnAllEvents() {
        eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        eventService.save(new Event("Football", null, 500.0, Rating.LOW));
        assertThat(eventService.getAll().size(), is(2));
    }

    @Test
    public void shouldRemoveEvent() {
        Event toKeep = eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        Event toDelete = eventService.save(new Event("Football", null, 500.0, Rating.LOW));
        eventService.remove(toDelete);
        assertThat(eventService.getAll().size(), is(1));
        assertThat(eventService.getAll().get(0), is(toKeep));
    }

    @Test
    public void shouldNotRemoveEventIfItDoesNotExist() {
        eventService.save(new Event("Concert", null, 1000.0, Rating.MID));
        eventService.save(new Event("Football", null, 500.0, Rating.LOW));
        Event toDelete = new Event("Concert", null, 1000.0, Rating.MID);
        assertThat(eventService.remove(toDelete), is(false));
        assertThat(eventService.getAll().size(), is(2));
        assertThat(eventService.getAll().get(0), not(toDelete));
    }

    @Test
    public void shouldFindEventsInMarch() {
        Auditorium auditorium = auditoriumService.getAll().get(0);
        TreeMap<LocalDateTime, Auditorium> concertTimetable = new TreeMap<>();
        concertTimetable.put(LocalDateTime.of(2020, 3, 1, 20, 0), auditorium);
        concertTimetable.put(LocalDateTime.of(2020, 4, 1, 20, 0), auditorium);
        Event concert = eventService.save(new Event("Concert", concertTimetable, 1000.0, Rating.MID));
        Event football = eventService.save(new Event("Football", LocalDateTime.of(2020, 3, 31, 19, 0), auditorium, 500.0, Rating.LOW));
        Event basketball = eventService.save(new Event("Basketball", LocalDateTime.of(2020, 3, 23, 19, 0), auditorium, 500.0, Rating.MID));
        eventService.save(new Event("F1", LocalDateTime.of(2020, 9, 21, 15, 0), auditorium, 4500.0, Rating.HIGH));
        eventService.save(new Event("Circus", LocalDateTime.of(2020, 2, 28, 18, 0), auditorium, 100.0, Rating.HIGH));

        List<Event> events = eventService.getForDateRange(LocalDate.of(2020, 3, 1), LocalDate.of(2020, 3, 31));
        assertThat(events.size(), equalTo(3));
        Map<String, Event>  eventsMap = mapEventsByName(events);
        assertThat(eventsMap.containsKey(concert.getName()), is(true));
        assertThat(eventsMap.containsKey(football.getName()), is(true));
        assertThat(eventsMap.containsKey(basketball.getName()), is(true));
        SortedMap<LocalDateTime, Auditorium> concertInMarch = eventsMap.get("Concert").getEventTimetable();
        assertThat(concertInMarch.size(), is(1));
        assertThat(concertInMarch.firstKey(), is(concertTimetable.firstKey()));
    }

    private static Map<String, Event> mapEventsByName(List<Event> events) {
        Map<String, Event> map = new HashMap<>();
        events.forEach(event -> map.put(event.getName(), event));
        return map;
    }

    @Test
    public void shouldFindAllEvents2MonthsFromNowOn() {
        LocalDate now = LocalDate.now();
        Auditorium auditorium = auditoriumService.getAll().get(0);
        Event concert = eventService.save(new Event("Concert", now.plusDays(25).atTime(20, 0), auditorium, 1000.0, Rating.MID));
        Event football = eventService.save(new Event("Football", now.plusDays(1).atStartOfDay(), auditorium, 500.0, Rating.LOW));
        Event basketball = eventService.save(new Event("Basketball", now.plusMonths(2).atTime(19, 0), auditorium, 500.0, Rating.MID));
        eventService.save(new Event("F1", now.minusDays(1).atTime(21, 0), auditorium, 4500.0, Rating.HIGH));
        eventService.save(new Event("Circus", now.plusMonths(2).plusDays(1).atTime(15, 0), auditorium, 100.0, Rating.HIGH));

        List<Event> events = eventService.getNextEvents(now.plusMonths(2));
        assertThat(events.size(), equalTo(3));
        Map<String, Event>  eventsMap = mapEventsByName(events);
        assertThat(eventsMap.containsKey(concert.getName()), is(true));
        assertThat(eventsMap.containsKey(football.getName()), is(true));
        assertThat(eventsMap.containsKey(basketball.getName()), is(true));
    }

    @Test
    public void shouldAddNewTimeToExistingEventsTimetable() {
        Auditorium auditorium = auditoriumService.getAll().get(0);
        TreeMap<LocalDateTime, Auditorium> concertTimetable = new TreeMap<>();
        concertTimetable.put(LocalDateTime.of(2020, 3, 1, 20, 0), auditorium);
        Event event = eventService.save(new Event("Concert", concertTimetable, 1000.0, Rating.MID));

        Event updatedEvent = eventService.addNewTimeForEvent(event.getId(), LocalDateTime.of(2020, 4, 1, 20, 0),
                auditoriumService.getAll().get(1));
        assertThat(eventService.getAll().size(), is(1)); // didn't add new event
        assertThat(updatedEvent.getEventTimetable().size(), is(2)); // added new time to timetable of existing event
    }
}
