package com.epam.spring.services;

import com.epam.spring.domain.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class EventServiceImpl implements EventService {
    private static AtomicLong ID_SEQUENCE = new AtomicLong(1);
    private static Map<Long, Event> events = new HashMap<>();

    @Override
    public Event save(Event event) {
        event.setId(ID_SEQUENCE.getAndIncrement());
        events.put(event.getId(), event);
        return event;
    }

    @Override
    public boolean remove(Event event) {
        return events.remove(event.getId()) != null;
    }

    @Override
    public Event getById(long id) {
        return events.get(id);
    }

    @Override
    public Event getByName(String name) {
        for (Event event : events.values()) {
            if (name.equals(event.getName())) {
                return event;
            }
        }
        return null;
    }

    @Override
    public List<Event> getAll() {
        return new ArrayList<>(events.values());
    }

    @Override
    public List<Event> getForDateRange(LocalDate from, LocalDate to) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            Event eventWithSuitableTimesOnly = null;
            for (LocalDateTime time : event.getEventTimetable().keySet()) {
                if (isAfterOrEqual(time.toLocalDate(), from) && isBeforeOrEqual(time.toLocalDate(), to)) {
                    if (eventWithSuitableTimesOnly == null) {
                        eventWithSuitableTimesOnly = new Event(event.getName(), new TreeMap<>(), event.getPrice(), event.getRating());
                        result.add(eventWithSuitableTimesOnly);
                    }
                    eventWithSuitableTimesOnly.getEventTimetable().put(time, event.getEventTimetable().get(time));
                }
            }
        }
        return result;
    }

    @Override
    public List<Event> getNextEvents(LocalDate to) {
        return getForDateRange(LocalDate.now(), to);
    }

    private static boolean isBeforeOrEqual(LocalDate date, LocalDate to) {
        return date.isBefore(to) || date.isEqual(to);
    }

    private static boolean isAfterOrEqual(LocalDate date, LocalDate from) {
        return date.isAfter(from) || date.isEqual(from);
    }

}
