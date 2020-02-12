package com.epam.spring.domain;

import java.time.LocalDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

public class Event {
    public static final double VIP_SEAT_CHARGE = 2; // probably should not be static but OK for now

    private Long id;
    private String name;
    private SortedMap<LocalDateTime, Auditorium> eventTimetable;
    private double price;
    private Rating rating;

    public Event(String name, SortedMap<LocalDateTime, Auditorium> eventTimetable, double price, Rating rating) {
        this.name = name;
        if (eventTimetable == null) {
            eventTimetable = new TreeMap<>();
        }
        this.eventTimetable = eventTimetable;
        this.price = price;
        this.rating = rating;
    }

    public Event(String name, LocalDateTime eventDate, Auditorium eventPlace, double price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.eventTimetable = new TreeMap<>();
        eventTimetable.put(eventDate, eventPlace);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedMap<LocalDateTime, Auditorium> getEventTimetable() {
        return eventTimetable;
    }

    public void setEventTimetable(SortedMap<LocalDateTime, Auditorium> eventTimetable) {
        this.eventTimetable = eventTimetable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", eventTimetable=" + eventTimetable +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }
}
