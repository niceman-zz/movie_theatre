package com.epam.spring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventCountersServiceImpl implements EventCountersService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addEventCounters(long eventId) {
        jdbcTemplate.update("insert into event_counters (event_id, name_counter, price_check_counter, book_counter) " +
                "values (?, 0, 0, 0)", eventId);
    }

    @Override
    public int getNameCounter(long eventId) {
        return getIntField("name_counter", eventId);
    }

    @Override
    public int getPriceCheckCounter(long eventId) {
        return getIntField("price_check_counter", eventId);
    }

    @Override
    public int getBookCounter(long eventId) {
        return getIntField("book_counter", eventId);
    }

    private int getIntField(String fieldName, long eventId) {
        Integer counter = DaoUtils.getByField("select " + fieldName + " from event_counters where event_id = ?", eventId,
                jdbcTemplate, Integer.class);
        if (counter == null) {
            return 0;
        }
        return counter;
    }

    @Override
    public void incrementNameCounter(long eventId) {
        updateCounter("name_counter", eventId);
    }

    @Override
    public void incrementPriceCheckCounter(long eventId) {
        updateCounter("price_check_counter", eventId);
    }

    @Override
    public void incrementBookCounter(long eventId) {
        updateCounter("book_counter", eventId);
    }

    private void updateCounter(String counterName, long eventId) {
        jdbcTemplate.update("update event_counters set " + counterName + " = " + counterName + " + 1 where event_id = " + eventId);
    }
}
