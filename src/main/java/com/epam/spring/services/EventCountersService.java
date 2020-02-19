package com.epam.spring.services;

public interface EventCountersService {
    void addEventCounters(long eventId);
    int getNameCounter(long eventId);
    int getPriceCheckCounter(long eventId);
    int getBookCounter(long eventId);
    void incrementNameCounter(long eventId);
    void incrementPriceCheckCounter(long eventId);
    void incrementBookCounter(long eventId);
}
