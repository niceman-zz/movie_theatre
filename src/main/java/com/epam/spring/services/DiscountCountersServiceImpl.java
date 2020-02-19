package com.epam.spring.services;

import com.epam.spring.discount.DiscountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DiscountCountersServiceImpl implements DiscountCountersService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void incrementDiscountForUser(DiscountStrategy strategy, long userId) {
        String discountName = strategy.getClass().getSimpleName();
        jdbcTemplate.update("merge into discount_counters dc using sysibm.sysdummy1 " +
                "on dc.discount_name = ? and dc.user_id = ? " +
                "when matched then " +
                "   update set dc.discount_counter = dc.discount_counter + 1 " +
                "when not matched then " +
                "   insert (discount_name, discount_counter, user_id) values (?, 1, ?)",
                discountName, userId, discountName, userId);
    }

    @Override
    public int getDiscountCounter(DiscountStrategy strategy) {
        try {
            Integer counter = jdbcTemplate.queryForObject(
                    "select sum(discount_counter) from discount_counters where discount_name = ?",
                    new Object[]{strategy.getClass().getSimpleName()}, Integer.class);
            if (counter != null) {
                return counter;
            }
        } catch (EmptyResultDataAccessException ignore) {}
        return 0;
    }

    @Override
    public int getDiscountCounterByUser(DiscountStrategy strategy, long userId) {
        try {
            Integer counter = jdbcTemplate.queryForObject(
                    "select discount_counter from discount_counters where discount_name = ? and user_id = ?",
                    new Object[]{strategy.getClass().getSimpleName(), userId}, Integer.class);
            if (counter != null) {
                return counter;
            }
        } catch (EmptyResultDataAccessException ignore) {}
        return 0;
    }
}
