package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.*;
import com.epam.spring.exceptions.MovieTheatreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class BookingServiceImpl implements BookingService {
    private static final double HIGH_RANKED_EVENT_CHARGE = 1.2;

    private static final Map<Event, Map<LocalDateTime, List<Ticket>>> bookedTickets = new HashMap<>();

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

        Ticket ticketForData = tickets.iterator().next();
        Map<LocalDateTime, List<Ticket>> allTicketsForEvent =
                bookedTickets.computeIfAbsent(ticketForData.getEvent(), v -> new HashMap<>());
        List<Ticket> ticketsForThisEvent =
                allTicketsForEvent.computeIfAbsent(ticketForData.getEventTime(), v -> new ArrayList<>());
        ticketsForThisEvent.addAll(tickets);

        saveTicketsToOwner(tickets);
    }

    private static void checkOwner(Set<Ticket> tickets) {
        User owner = tickets.iterator().next().getOwner();
        for (Ticket ticket: tickets) {
            if (!owner.equals(ticket.getOwner())) {
                throw new MovieTheatreException("Can't book tickets for different users in one operation.");
            }
        }
    }

    private static void checkBookedTickets(Set<Ticket> tickets) {
        Ticket forData = tickets.iterator().next();
        List<Ticket> alreadyBooked = bookedTickets.getOrDefault(forData.getEvent(), Collections.emptyMap()).get(forData.getEventTime());
        if (alreadyBooked == null || alreadyBooked.isEmpty()) {
            return;
        }
        List<Integer> bookedSeats = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (alreadyBooked.contains(ticket)) {
                bookedSeats.add(ticket.getSeat());
            }
        }
        if (!bookedSeats.isEmpty()) {
            throw new MovieTheatreException("Can't book tickets as these tickets are already booked: " + bookedSeats);
        }
    }

    private void saveTicketsToOwner(Set<Ticket> tickets) {
        User owner = tickets.iterator().next().getOwner();
        if (userService.isRegistered(owner)) {
            User fromStorage = userService.getById(owner.getId());
            fromStorage.getTickets().addAll(tickets);
            if (owner != fromStorage) {
                owner.getTickets().addAll(tickets);
            }
        } else {
            owner.getTickets().addAll(tickets);
        }
    }

    @Override
    public List<Ticket> getPurchasedTicketsForEvent(Event event, LocalDateTime dateTime) {
        Map<LocalDateTime, List<Ticket>> allTicketsForEvent = bookedTickets.get(event);
        if (allTicketsForEvent == null || allTicketsForEvent.isEmpty()) {
            return Collections.emptyList();
        }
        List<Ticket> particularDateTickets = allTicketsForEvent.get(dateTime);
        if (particularDateTickets == null) {
            return Collections.emptyList();
        }
        return particularDateTickets;
    }

    @Override
    public void clear() {
        bookedTickets.clear();
    }
}
