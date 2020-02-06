package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class DiscountServiceImpl implements DiscountService {
    private List<DiscountStrategy> discountStrategies;

    public void setDiscountStrategies(List<DiscountStrategy> discountStrategies) {
        this.discountStrategies = discountStrategies;
    }

    @Override
    public int getDiscount(Event event, LocalDateTime dateTime, User user, Set<Integer> seats) {
        int discount = 0;
        for (DiscountStrategy strategy: discountStrategies) {
            if (strategy.isEligible(user, seats.size(), dateTime)) {
                discount = Math.max(discount, calculateEffectiveDiscount(strategy));
            }
        }
        return discount;
    }

    private int calculateEffectiveDiscount(DiscountStrategy strategy) {
        if (strategy.isDiscountAll()) {
            return strategy.getDiscount();
        }
        return strategy.getDiscount() / strategy.getDiscountThreshold(); // it's incorrect but just for simplicity
    }
}
