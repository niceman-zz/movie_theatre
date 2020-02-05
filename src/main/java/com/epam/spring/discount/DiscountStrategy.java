package com.epam.spring.discount;

import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public abstract class DiscountStrategy {
    private int discount;
    private boolean discountAll;
    private int discountThreshold;

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public boolean isDiscountAll() {
        return discountAll;
    }

    public void setDiscountAll(boolean discountAll) {
        this.discountAll = discountAll;
    }

    public int getDiscountThreshold() {
        return discountThreshold;
    }

    public void setDiscountThreshold(int discountThreshold) {
        this.discountThreshold = discountThreshold;
    }

    public abstract boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime);
}
