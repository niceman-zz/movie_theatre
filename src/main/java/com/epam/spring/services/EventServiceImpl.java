package com.epam.spring.services;

import com.epam.spring.aspects.CounterAspect;
import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.rowmappers.EventRowMapper;
import com.epam.spring.exceptions.MovieTheatreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Transactional
public class EventServiceImpl implements EventService {
    private static final String BASE_SELECT = "select id, name, price, rating from events";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditoriumService auditoriumService;

    @Autowired
    private EventCountersService eventCountersService;

    @Override
    public Event save(Event event) {
        if (getByName(event.getName()) != null) {
            throw new MovieTheatreException("Event with this name already exists: " + event.getName());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into events (name, price, rating) values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getName());
            ps.setDouble(2, event.getPrice());
            ps.setString(3, event.getRating().name());
            return ps;
        }, keyHolder);

        event.setId(keyHolder.getKey().longValue());

        saveTimetable(event);
        eventCountersService.addEventCounters(event.getId());

        return event;
    }

    private void saveTimetable(Event event) {
        if (event.getEventTimetable().isEmpty()) {
            return;
        }
        Iterator<Map.Entry<LocalDateTime, Auditorium>> it = event.getEventTimetable().entrySet().iterator();
        jdbcTemplate.batchUpdate("insert into event_timetables (event_id, event_time, auditorium) values (?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        Map.Entry<LocalDateTime, Auditorium> specificTime = it.next();
                        preparedStatement.setLong(1, event.getId());
                        preparedStatement.setTimestamp(2, Timestamp.valueOf(specificTime.getKey()));
                        preparedStatement.setString(3, specificTime.getValue().getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return event.getEventTimetable().size();
                    }
                });
    }

    @Override
    public boolean remove(Event event) {
        return jdbcTemplate.update("delete from events where id = ?", event.getId()) > 0;
    }

    @Override
    public Event getById(long id) {
        return getByField("id", id);
    }

    @Override
    public Event getByName(String name) {
        return getByField("name", name);
    }

    private Event getByField(String fieldName, Object fieldValue) {
        Event event = DaoUtils.getByField(
                BASE_SELECT + " where " + fieldName + " = ?", fieldValue, jdbcTemplate, new EventRowMapper());
        if (event != null) {
            event.setEventTimetable(getEventTimetable(event.getId()));
        }
        return event;
    }

    private TreeMap<LocalDateTime, Auditorium> getEventTimetable(long id) {
        TreeMap<LocalDateTime, Auditorium> timetable = new TreeMap<>();
        jdbcTemplate.query("select event_time, auditorium from event_timetables where event_id = ?", new Object[]{id},
                (rs) -> {
                    LocalDateTime time = rs.getTimestamp("event_time").toLocalDateTime();
                    String auditorium = rs.getString("auditorium");
                    timetable.put(time, auditoriumService.getByName(auditorium));
                });
        return timetable;
    }

    @Override
    public List<Event> getAll() {
        List<Event> events = jdbcTemplate.query(BASE_SELECT, new EventRowMapper());
        events.forEach(event -> event.setEventTimetable(getEventTimetable(event.getId())));
        return events;
    }

    @Override
    public List<Event> getForDateRange(LocalDate from, LocalDate to) {
        String select = "select e.name, e.price, e.rating, et.event_time, et.auditorium " +
                "from events e, event_timetables et " +
                "where e.id = et.event_id and et.event_time >= ? and et.event_time <= ?" +
                "order by et.event_time asc";
        List<Event> events = jdbcTemplate.query(select,
                new Object[] {Timestamp.valueOf(from.atTime(0, 0, 0, 0)), Timestamp.valueOf(to.atTime(23, 59, 59, 999_999_999))},
                (rs, index) -> {
                    Event event = new Event();
                    event.setName(rs.getString("name"));
                    event.setPrice(rs.getDouble("price"));
                    event.setRating(Rating.valueOf(rs.getString("rating")));
                    LocalDateTime time = rs.getTimestamp("event_time").toLocalDateTime();
                    String auditorium = rs.getString("auditorium");
                    event.getEventTimetable().put(time, auditoriumService.getByName(auditorium));

                    return event;
                });
        return events;
    }

    @Override
    public List<Event> getNextEvents(LocalDate to) {
        return getForDateRange(LocalDate.now(), to);
    }

    @Override
    public Event addNewTimeForEvent(Long eventId, LocalDateTime time, Auditorium auditorium) {
        jdbcTemplate.update("insert into event_timetables (event_id, event_time, auditorium) values (?, ?, ?)",
                eventId, Timestamp.valueOf(time), auditorium.getName());
        return getById(eventId);
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from events");
    }
}
