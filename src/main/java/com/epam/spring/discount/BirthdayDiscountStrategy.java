package com.epam.spring.discount;

import com.epam.spring.domain.User;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.DAYS;

public class BirthdayDiscountStrategy extends DiscountStrategy {
    @Override
    public boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime) {
        return Math.abs(DAYS.between(user.getBirthday(), eventTime)) <= getDiscountThreshold();
    }
}
