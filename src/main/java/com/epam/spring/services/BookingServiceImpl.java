package com.epam.spring.services;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Ticket;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class BookingServiceImpl implements BookingService {
    private final DiscountService discountService;

    BookingServiceImpl(DiscountService discountService) {
        this.discountService = discountService;
    }

    @Override
    public double getTicketsPrice(Event event, LocalDateTime eventTime, User user, Set<Integer> seats) {

        return 0;
    }

    @Override
    public double bookTickets(Set<Ticket> tickets) {
        return 0;
    }

    @Override
    public List<Ticket> getPurchasedTicketsForEvent(Event event, LocalDateTime dateTime) {
        return null;
    }
}
