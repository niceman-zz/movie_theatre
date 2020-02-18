package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.*;
import com.epam.spring.domain.rowmappers.TicketRowMapper;
import com.epam.spring.exceptions.MovieTheatreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Transactional
public class BookingServiceImpl implements BookingService {
    private static final double HIGH_RANKED_EVENT_CHARGE = 1.2;

    private static final Map<Event, Map<LocalDateTime, List<Ticket>>> bookedTickets = new HashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private UserService userService;

    @Override
    public double getTicketsPrice(Event event, LocalDateTime eventTime, User user, Set<Integer> seats) {
        if (seats == null || seats.isEmpty()) {
            return 0;
        }
        Auditorium auditorium = event.getEventTimetable().get(eventTime);
        int numOfVipSeats = findVipSeats(auditorium, seats);
        DiscountStrategy discountStrategy = discountService.getDiscount(event, eventTime, user, seats);
        int discount = discountStrategy.getEffectiveDiscount(auditorium, seats);
        double basePrice = event.getPrice();
        double eventCharge = event.getRating() == Rating.HIGH ? HIGH_RANKED_EVENT_CHARGE : 1;

        double ordinaryTickets = basePrice * eventCharge * (seats.size() - numOfVipSeats);
        double vipTickets = basePrice * Event.VIP_SEAT_CHARGE * eventCharge * numOfVipSeats;

        return (ordinaryTickets + vipTickets) * (100 - discount) / 100;
    }

    private static int findVipSeats(Auditorium auditorium, Set<Integer> seats) {
        Set<Integer> vipSeats = auditorium.getVipSeats();
        int vipNum = 0;
        for (Integer seat : seats) {
            if (vipSeats.contains(seat)) {
                vipNum++;
            }
        }
        return vipNum;
    }

    @Override
    public void bookTickets(Set<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        checkOwner(tickets);
        checkBookedTickets(tickets);

        User user = tickets.iterator().next().getOwner();
        if (user.isLucky()) {
            user.setLucky(false); // just in case
            StringBuilder message = new StringBuilder("You've got your tickets for free! List of the tickets:\n");
            tickets.forEach(message::append);
            saveLuckyWinner(user.getId(), message.toString());
            user.getLuckyWinnerMessages().add(message.toString());

            System.out.println("You're lucky bastard! Get your tickets for free!!!"); // nothing else as we don't actually book tickets
        }

        saveTickets(tickets);
        user.getTickets().addAll(tickets);
    }

    private static void checkOwner(Set<Ticket> tickets) {
        User owner = tickets.iterator().next().getOwner();
        for (Ticket ticket: tickets) {
            if (!owner.equals(ticket.getOwner())) {
                throw new MovieTheatreException("Can't book tickets for different users in one operation.");
            }
        }
    }

    private void checkBookedTickets(Set<Ticket> tickets) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        tickets.forEach(ticket -> joiner.add(String.valueOf(ticket.getSeat())));

        String query = "select t.seat " +
                "from tickets t, events e, users u " +
                "where t.event_id = e.id and t.user_id = u.id and t.event_id = ? and t.event_time = ? and t.seat in " +
                joiner.toString();

        Ticket forData = tickets.iterator().next();
        List<Integer> bookedSeats = jdbcTemplate.queryForList(query,
                new Object[] {forData.getEvent().getId(), Timestamp.valueOf(forData.getEventTime())}, Integer.class);
        if (!bookedSeats.isEmpty()) {
            throw new MovieTheatreException("Can't book tickets as these tickets are already booked: " + bookedSeats);
        }
    }

    private void saveTickets(Set<Ticket> tickets) {
        Iterator<Ticket> it = tickets.iterator();
        jdbcTemplate.batchUpdate("insert into tickets (event_id, event_time, seat, user_id) values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        Ticket ticket = it.next();
                        preparedStatement.setLong(1, ticket.getEvent().getId());
                        preparedStatement.setTimestamp(2, Timestamp.valueOf(ticket.getEventTime()));
                        preparedStatement.setInt(3, ticket.getSeat());
                        preparedStatement.setLong(4, ticket.getOwner().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return tickets.size();
                    }
                });
    }

    private void saveLuckyWinner(long userId, String message) {
        jdbcTemplate.update("insert into lucky_winners (user_id, message) values (?, ?)", userId, message);
    }

    @Override
    public List<Ticket> getPurchasedTicketsForEvent(Event event, LocalDateTime dateTime) {
        String query = "select t.event_id, t.event_time, t.seat, t.user_id, " +
                "e.name as event_name, e.price as event_price, e.rating as event_rating, " +
                "u.first_name as user_first_name, u.last_name as user_last_name, u.email as user_email, u.birthday as user_birthday " +
                "from tickets t, events e, users u " +
                "where t.event_id = e.id and t.user_id = u.id and t.event_id = ? and t.event_time = ?";
        return jdbcTemplate.query(query, new Object[] {event.getId(), Timestamp.valueOf(dateTime)}, new TicketRowMapper());
    }

    @Override
    public void clear() {
        bookedTickets.clear();
    }
}
