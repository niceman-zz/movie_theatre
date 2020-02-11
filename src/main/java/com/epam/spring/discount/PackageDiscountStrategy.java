package com.epam.spring.discount;

import com.epam.spring.domain.User;

import java.time.LocalDateTime;

public class PackageDiscountStrategy extends DiscountStrategy {
    public PackageDiscountStrategy(int discount, int discountThreshold, boolean discountAll) {
        super(discount, discountThreshold, discountAll);
    }

    @Override
    public boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime) {
        return numOfSeats >= getDiscountThreshold();
    }
}
