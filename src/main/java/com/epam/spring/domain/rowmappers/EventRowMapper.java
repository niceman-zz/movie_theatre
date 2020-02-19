package com.epam.spring.domain.rowmappers;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int i) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getLong("id"));
        event.setName(resultSet.getString("name"));
        event.setPrice(resultSet.getDouble("price"));
        event.setRating(Rating.valueOf(resultSet.getString("rating")));

        return event;
    }
}
