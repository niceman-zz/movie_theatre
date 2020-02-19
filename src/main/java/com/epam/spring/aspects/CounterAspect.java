package com.epam.spring.aspects;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Ticket;
import com.epam.spring.services.EventCountersService;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Aspect
@Component
public class CounterAspect {
    @Autowired
    private EventCountersService eventCountersService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Pointcut("execution(public * com.epam.spring.services.EventService+.getByName(..))")
    private void eventByName() {
    }

    @Pointcut("execution(public * com.epam.spring.services.BookingService+.getTicketsPrice(..))")
    private void eventCheckPrice() {
    }

    @Pointcut("execution(public * com.epam.spring.services.BookingService+.bookTickets(..))")
    private void eventBooked() {
    }

    @AfterReturning(pointcut = "eventByName()", returning = "event")
    public void countEventByName(Event event) {
        if (event != null) {
            eventCountersService.incrementNameCounter(event.getId());
        }
    }

    @Before("eventCheckPrice() && args(event,..)")
    public void countEventsByPriceCheck(Event event) {
        eventCountersService.incrementPriceCheckCounter(event.getId());
    }

    @After("eventBooked() && args(tickets)")
    public void countEventBooking(Set<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        eventCountersService.incrementBookCounter(tickets.iterator().next().getEvent().getId());
    }
}
