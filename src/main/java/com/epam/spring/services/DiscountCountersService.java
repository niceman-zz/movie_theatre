package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;

public interface DiscountCountersService {
    void incrementDiscountForUser(DiscountStrategy strategy, long userId);
    int getDiscountCounter(DiscountStrategy strategy);
    int getDiscountCounterByUser(DiscountStrategy strategy, long userId);
}
