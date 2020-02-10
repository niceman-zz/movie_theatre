package com.epam.spring.domain;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "Ticket{" +
                "event=" + event.getName() +
                ", seat=" + seat +
                ", eventTime=" + eventTime +
                ", owner=" + owner.getFullName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return seat == ticket.seat &&
                Objects.equals(event, ticket.event) &&
                Objects.equals(eventTime, ticket.eventTime) &&
                Objects.equals(owner, ticket.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, seat, eventTime, owner);
    }
}
