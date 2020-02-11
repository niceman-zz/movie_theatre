package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class EventServiceImpl implements EventService {
    private static AtomicLong ID_SEQUENCE = new AtomicLong(1);
    private static Map<Long, Event> events = new HashMap<>();

    @Override
    public Event save(Event event) {
        if (getByName(event.getName()) != null) {
            throw new IllegalArgumentException("Event with this name already exists: " + event.getName());
        }
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
            for (LocalDateTime time : event.getEventTimetable().keySet()) {
                if (isAfterOrEqual(time.toLocalDate(), from) && isBeforeOrEqual(time.toLocalDate(), to)) {
                    result.add(new Event(event.getName(), time, event.getEventTimetable().get(time), event.getPrice(),
                            event.getRating()));
                }
            }
        }
        return result;
    }

    @Override
    public List<Event> getNextEvents(LocalDate to) {
        return getForDateRange(LocalDate.now(), to);
    }

    @Override
    public Event addNewTimeForEvent(Long eventId, LocalDateTime time, Auditorium auditorium) {
        if (!events.containsKey(eventId)) {
            throw new IllegalArgumentException("There's no event with ID " + eventId + " in the system");
        }
        Event event = events.get(eventId);
        event.getEventTimetable().put(time, auditorium);
        return event;
    }

    private static boolean isBeforeOrEqual(LocalDate date, LocalDate to) {
        return date.isBefore(to) || date.isEqual(to);
    }

    private static boolean isAfterOrEqual(LocalDate date, LocalDate from) {
        return date.isAfter(from) || date.isEqual(from);
    }

    @Override
    public void clear() {
        events.clear();
    }
}
