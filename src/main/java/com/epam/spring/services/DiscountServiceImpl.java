package com.epam.spring.services;

import com.epam.spring.aspects.DiscountAspect;
import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.discount.NoDiscountStrategy;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class DiscountServiceImpl implements DiscountService {
    @Autowired
    private List<DiscountStrategy> discountStrategies;

    @Autowired
    private DiscountAspect discountAspect;

    @Override
    public DiscountStrategy getDiscount(Event event, LocalDateTime dateTime, User user, Set<Integer> seats) {
        int discount = 0;
        DiscountStrategy resultingStrategy = NoDiscountStrategy.instance(); // just for the sake of the task with aspects
        for (DiscountStrategy strategy: discountStrategies) {
            if (strategy.isEligible(user, seats.size(), dateTime)) {
                int newDiscount = strategy.getEffectiveDiscount(event.getEventTimetable().get(dateTime), seats);
                if (discount < newDiscount) {
                    discount = newDiscount;
                    resultingStrategy = strategy;
                }
            }
        }
        return resultingStrategy;
    }

    @Override
    public List<DiscountStrategy> getAllStrategies() {
        return discountStrategies;
    }

    @Override
    public int getDiscountCounter(DiscountStrategy strategy) {
        return discountAspect.getDiscountsCounter(strategy);
    }

    @Override
    public int getDiscountCounterByUser(DiscountStrategy strategy, User user) {
        return discountAspect.getDiscountsCounterByUser(strategy, user);
    }
}
