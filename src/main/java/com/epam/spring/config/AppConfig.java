package com.epam.spring.config;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.epam.spring")
@Import({DiscountsConfig.class, DbConfig.class})
@EnableAspectJAutoProxy
public class AppConfig {
    @Bean
    public static Properties auditoriumProperties() throws IOException {
        PropertiesFactoryBean factory = new PropertiesFactoryBean();
        factory.setLocations(new ClassPathResource("auditoriums.properties"),
                new ClassPathResource("additional-auditoriums.properties"));
        factory.setFileEncoding("UTF-8");
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
