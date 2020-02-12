package com.epam.spring.discount;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class DiscountStrategy {
    private final int discount;
    private final int discountThreshold;
    private final boolean discountAll;

    public DiscountStrategy(int discount, int discountThreshold, boolean discountAll) {
        this.discount = discount;
        this.discountThreshold = discountThreshold;
        this.discountAll = discountAll;
    }

    public int getDiscount() {
        return discount;
    }

    public boolean isDiscountAll() {
        return discountAll;
    }

    public int getDiscountThreshold() {
        return discountThreshold;
    }

    public abstract boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime);

    public int getEffectiveDiscount(Auditorium auditorium, Set<Integer> seats) {
        return discount;
    }
}
