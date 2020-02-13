package com.epam.spring.discount;

import com.epam.spring.domain.User;

import java.time.LocalDateTime;

public class NoDiscountStrategy extends DiscountStrategy {
    private final static NoDiscountStrategy INSTANCE = new NoDiscountStrategy();

    private NoDiscountStrategy() {
        super(0, 0, false);
    }

    @Override
    public boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime) {
        return true;
    }

    public static DiscountStrategy getInstance() {
        return INSTANCE;
    }
}
