package com.epam.spring.discount;

import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public class CompoundDiscountStrategy extends DiscountStrategy {
    private List<DiscountStrategy> discountStrategies;

    public CompoundDiscountStrategy(int discount, int discountThreshold, boolean discountAll,
                                    List<DiscountStrategy> discountStrategies) {
        super(discount, discountThreshold, discountAll);
        this.discountStrategies = discountStrategies;
    }

    @Override
    public boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime) {
        for (DiscountStrategy strategy : discountStrategies) {
            if (!strategy.isEligible(user, numOfSeats, eventTime)) {
                return false;
            }
        }
        return true;
    }

    public List<DiscountStrategy> getDiscountStrategies() {
        return discountStrategies;
    }

    public void setDiscountStrategies(List<DiscountStrategy> discountStrategies) {
        this.discountStrategies = discountStrategies;
    }
}
