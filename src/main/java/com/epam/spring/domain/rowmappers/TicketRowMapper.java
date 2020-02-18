package com.epam.spring.domain.rowmappers;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Rating;
import com.epam.spring.domain.Ticket;
import com.epam.spring.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TicketRowMapper implements RowMapper<Ticket> {
    @Override
    public Ticket mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setFirstName(resultSet.getString("user_first_name"));
        user.setLastName(resultSet.getString("user_last_name"));
        user.setEmail(resultSet.getString("user_email"));
        user.setBirthday(resultSet.getDate("user_birthday").toLocalDate());

        Event event = new Event();
        event.setId(resultSet.getLong("event_id"));
        event.setName(resultSet.getString("event_name"));
        event.setPrice(resultSet.getDouble("event_price"));
        event.setRating(Rating.valueOf(resultSet.getString("event_rating")));

        int seat = resultSet.getInt("seat");
        LocalDateTime eventTime = resultSet.getTimestamp("event_time").toLocalDateTime();

        return new Ticket(event, seat, eventTime, user);
    }
}
