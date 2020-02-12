package com.epam.spring.discount;

import com.epam.spring.domain.Auditorium;
import com.epam.spring.domain.Event;
import com.epam.spring.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

public class PackageDiscountStrategy extends DiscountStrategy {
    public PackageDiscountStrategy(int discount, int discountThreshold, boolean discountAll) {
        super(discount, discountThreshold, discountAll);
    }

    @Override
    public boolean isEligible(User user, int numOfSeats, LocalDateTime eventTime) {
        return numOfSeats >= getDiscountThreshold();
    }

    @Override
    public int getEffectiveDiscount(Auditorium auditorium, Set<Integer> seats) {
        if (isDiscountAll()) {
            return getDiscount();
        }
        int vipSeatsNum = 0;
        for (Integer seat : seats) {
            if (auditorium.getVipSeats().contains(seat)) {
                vipSeatsNum++;
            }
        }
        int ordinarySeatsNum = seats.size() - vipSeatsNum;

        int vipDiscounts = vipSeatsNum / getDiscountThreshold();
        int ordinaryDiscounts = seats.size() / getDiscountThreshold() - vipDiscounts;

        double totalPrice = vipSeatsNum * Event.VIP_SEAT_CHARGE + ordinarySeatsNum; // price "in weights"

        double vipDiscountedPrice = vipDiscounts * Event.VIP_SEAT_CHARGE * getDiscount() / 100;
        double ordinaryDiscountedPrice = (double) ordinaryDiscounts * getDiscount() / 100;

        double finalDiscount = (vipDiscountedPrice + ordinaryDiscountedPrice) * 100 / totalPrice;

        return (int) Math.round(finalDiscount);
    }
}
