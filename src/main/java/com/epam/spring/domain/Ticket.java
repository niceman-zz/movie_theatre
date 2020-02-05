package com.epam.spring.domain;

import java.time.LocalDateTime;

public class Ticket {
    private Event event;
    private int seat;
    private LocalDateTime eventTime;
    private User owner;

    public Ticket(Event event, int seat, LocalDateTime eventTime, User owner) {
        this.event = event;
        this.seat = seat;
        this.eventTime = eventTime;
        this.owner = owner;
    }

    public Event getEvent() {
        return event;
    }

    public int getSeat() {
        return seat;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public User getOwner() {
        return owner;
    }
}
