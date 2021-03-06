package com.epam.spring.services;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Ticket;
import com.epam.spring.domain.User;
import com.epam.spring.exceptions.AlreadyBookedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingService {
    static final double HIGH_RANKED_EVENT_CHARGE = 1.2;

    double getTicketsPrice(Event event, LocalDateTime eventTime, User user, Set<Integer> seats);
    void bookTickets(Set<Ticket> tickets) throws AlreadyBookedException;
    List<Ticket> getPurchasedTicketsForEvent(Event event, LocalDateTime dateTime);
    void clear();
}
