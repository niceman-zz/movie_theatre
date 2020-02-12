package com.epam.spring.aspects;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.Ticket;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class CounterAspect {
    private Map<String, Integer> eventCounter = new HashMap<>();
    private Map<String, Integer> eventPriceChecks = new HashMap<>();
    private Map<String, Integer> eventBooked = new HashMap<>();

    @Pointcut("execution(public * com.epam.spring.services.EventService+.getByName(..))")
    private void eventByName() {
    }

    @Pointcut("execution(public * com.epam.spring.services.BookingService+.getTicketsPrice(..))")
    private void eventCheckPrice() {
    }

    @Pointcut("execution(public * com.epam.spring.services.BookingService+.bookTickets(..))")
    private void eventBooked() {
    }

    @Before("eventByName() && args(name)")
    public void countEventByName(String name) {
        eventCounter.merge(name, 1, Integer::sum);
    }

    @Before("eventCheckPrice() && args(event,..)")
    public void countEventsByPriceCheck(Event event) {
        eventPriceChecks.merge(event.getName(), 1, Integer::sum);
    }

    @After("eventBooked() && args(tickets)")
    public void countEventBooking(Set<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        eventBooked.merge(tickets.iterator().next().getEvent().getName(), 1, Integer::sum);
    }

    public int getEventCount(String eventName) {
        return eventCounter.getOrDefault(eventName, 0);
    }

    public int getEventPriceChecks(String eventName) {
        return eventPriceChecks.getOrDefault(eventName, 0);
    }

    public int getEventBookingsCount(String eventName) {
        return eventBooked.getOrDefault(eventName, 0);
    }
}
