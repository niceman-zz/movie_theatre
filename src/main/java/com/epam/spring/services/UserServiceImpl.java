package com.epam.spring.services;

import com.epam.spring.domain.Ticket;
import com.epam.spring.domain.User;
import com.epam.spring.domain.rowmappers.TicketRowMapper;
import com.epam.spring.domain.rowmappers.UserRowMapper;
import com.epam.spring.exceptions.MovieTheatreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
public class UserServiceImpl implements UserService {
    private static final String BASE_SELECT = "select id, first_name, last_name, email, birthday from users";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        if (getByEmail(user.getEmail()) != null) {
            throw new MovieTheatreException("User with this email already exists: " + user.getEmail());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into users (first_name, last_name, email, birthday) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());

        return user;
    }

    @Override
    public void update(User user) {
        int updated = jdbcTemplate.update("update users set first_name = ?, last_name = ?, email = ?, birthday = ? where id = ?",
                user.getFirstName(), user.getLastName(), user.getEmail(), Date.valueOf(user.getBirthday()), user.getId());
        if (updated == 0) {
            throw new MovieTheatreException("Can't update unregistered user: " + user.getFullName());
        }
    }

    @Override
    public boolean remove(User user) {
        return jdbcTemplate.update("delete from users where id = ?", user.getId()) > 0;
    }

    @Override
    public User getById(long id) {
        return getByField("id", id);
    }

    @Override
    public User getByEmail(String email) {
        return getByField("email", email);
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query(BASE_SELECT, new UserRowMapper());
    }

    private User getByField(String fieldName, Object fieldValue) {
        User user = DaoUtils.getByField(BASE_SELECT + " where " + fieldName + " = ?", fieldValue, jdbcTemplate, new UserRowMapper());
        if (user == null) {
            return null;
        }
        user.getTickets().addAll(getTickets(user.getId()));
        user.getLuckyWinnerMessages().addAll(getLuckyWinnerMessages(user.getId()));
        return user;
    }

    private List<Ticket> getTickets(long userId) {
        String query = "select t.event_id, t.event_time, t.seat, t.user_id, " +
                "e.name as event_name, e.price as event_price, e.rating as event_rating, " +
                "u.first_name as user_first_name, u.last_name as user_last_name, u.email as user_email, u.birthday as user_birthday " +
                "from tickets t, events e, users u " +
                "where t.event_id = e.id and t.user_id = u.id and t.user_id = " + userId;
        return jdbcTemplate.query(query, new TicketRowMapper());
    }

    private List<String> getLuckyWinnerMessages(long userId) {
        return jdbcTemplate.queryForList("select message from lucky_winners where user_id = " + userId, String.class);
    }

    @Override
    public boolean isRegistered(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        Integer userCount = jdbcTemplate.queryForObject("select count(id) from users where id = ?",
                new Object[] {user.getId()}, Integer.class);
        return userCount == 1;
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from users");
    }
}
