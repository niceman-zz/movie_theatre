package com.epam.spring.aspects;

import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.discount.NoDiscountStrategy;
import com.epam.spring.domain.User;
import com.epam.spring.services.DiscountCountersService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DiscountAspect {
    @Autowired
    private DiscountCountersService discountCountersService;

    @Pointcut("execution(public * com.epam.spring.services.DiscountService+.getDiscount(..))")
    private void getDiscount() {
    }

    @AfterReturning(pointcut = "getDiscount()", returning = "strategy")
    public void countDiscounts(JoinPoint joinPoint, DiscountStrategy strategy) {
        if (strategy != NoDiscountStrategy.instance()) {
            System.out.println("You've got discount! (" + strategy.getClass().getSimpleName() + ")");
        }
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof User) {
                discountCountersService.incrementDiscountForUser(strategy, ((User) arg).getId());
                return;
            }
        }

    }

    public int getDiscountsCounter(DiscountStrategy strategy) {
        return discountCountersService.getDiscountCounter(strategy);
    }

    public int getDiscountsCounterByUser(DiscountStrategy strategy, User user) {
        return discountCountersService.getDiscountCounterByUser(strategy, user.getId());
    }
}