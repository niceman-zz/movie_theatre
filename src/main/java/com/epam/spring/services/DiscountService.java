package com.epam.spring.services;

import com.epam.spring.domain.Event;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

public interface DiscountService {
    int getDiscount(Event event, LocalDateTime dateTime, User user, Set<Integer> seats);
}
