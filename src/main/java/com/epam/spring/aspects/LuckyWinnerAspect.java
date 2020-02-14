package com.epam.spring.aspects;

import com.epam.spring.domain.Ticket;
import com.epam.spring.domain.User;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;

@Component
@Aspect
public class LuckyWinnerAspect {
    @Pointcut("execution(public * com.epam.spring.services.BookingService+.bookTickets(..))")
    private void bookTicketsPoint() {
    }

    @Before("bookTicketsPoint() && args(tickets)")
    public void checkForLuck(Set<Ticket> tickets) {
        System.out.println("---===checkForLuck===---");
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        User user = tickets.iterator().next().getOwner();
        user.setLucky(checkLucky());
    }

    private boolean checkLucky() {
        return new Random().nextInt(10) >= 8; // ~20% chance
    }
}
