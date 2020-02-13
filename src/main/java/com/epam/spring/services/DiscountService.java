package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DiscountService {
    DiscountStrategy getDiscount(Event event, LocalDateTime dateTime, User user, Set<Integer> seats);
    List<DiscountStrategy> getAllStrategies();
    int getDiscountCounter(DiscountStrategy strategy);
    int getDiscountCounterByUser(DiscountStrategy strategy, User user);
}
