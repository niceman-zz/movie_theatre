package com.epam.spring.aspects;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.domain.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class DiscountAspect {
    private final Map<DiscountStrategy, Map<User, Integer>> discountsCounter = new HashMap<>();

    @Pointcut("execution(public * com.epam.spring.services.DiscountService+.getDiscount(..))")
    private void getDiscount() {
    }

    @AfterReturning(pointcut = "getDiscount()", returning = "strategy")
    public void countDiscounts(JoinPoint joinPoint, DiscountStrategy strategy) {
        if (!discountsCounter.containsKey(strategy)) {
            discountsCounter.put(strategy, new HashMap<>());
        }
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof User) {
                Map<User, Integer> userCounter = discountsCounter.get(strategy);
                userCounter.merge((User) arg, 1, Integer::sum);
                return;
            }
        }

    }

    public int getDiscountsCounter(DiscountStrategy strategy) {
        return discountsCounter.getOrDefault(strategy, Collections.emptyMap()).size();
    }

    public int getDiscountsCounterByUser(DiscountStrategy strategy, User user) {
        return discountsCounter.getOrDefault(strategy, Collections.emptyMap()).getOrDefault(user, 0);
    }
}