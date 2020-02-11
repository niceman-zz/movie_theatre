package com.epam.spring.config;

import com.epam.spring.discount.BirthdayDiscountStrategy;
import com.epam.spring.discount.CompoundDiscountStrategy;
import com.epam.spring.discount.DiscountStrategy;
import com.epam.spring.discount.PackageDiscountStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DiscountsConfig {
    @Bean
    public DiscountStrategy birthdayDiscountStrategy() {
        return new BirthdayDiscountStrategy(10, 5, true);
    }

    @Bean
    public DiscountStrategy packageDiscountStrategy() {
        return new PackageDiscountStrategy(5, 10, false);
    }

    @Bean
    public DiscountStrategy birthdayPackageDiscountStrategy() {
        List<DiscountStrategy> strategies = new ArrayList<>();
        strategies.add(birthdayDiscountStrategy());
        strategies.add(packageDiscountStrategy());
        return new CompoundDiscountStrategy(30, 0, true, strategies);
    }
}
