package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    Event save(Event event);
    boolean remove(Event event);
    Event getById(long id);
    Event getByName(String name);
    List<Event> getAll();
    List<Event> getForDateRange(LocalDate from, LocalDate to);
    List<Event> getNextEvents(LocalDate to);
    Event addNewTimeForEvent(Long eventId, LocalDateTime time, Auditorium auditorium);
    void clear();
}
