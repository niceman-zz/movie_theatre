package com.epam.spring.config;

import com.epam.spring.services.AuditoriumService;
import com.epam.spring.services.AuditoriumServiceImpl;
import com.epam.spring.services.UserService;
import com.epam.spring.services.UserServiceImpl;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.epam.spring")
@Import({DiscountsConfig.class})
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
