package com.epam.spring.services;

import com.epam.spring.config.AppConfig;
import com.epam.spring.domain.Auditorium;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class AuditoriumServiceTest {
    @Autowired
    private AuditoriumService auditoriumService;

    @Test
    public void shouldGetAllAuditoriums() {
        List<Auditorium> auditoriums = auditoriumService.getAll();
        assertThat(auditoriums.size(), is(4));
    }

    @Test
    public void shouldGetByNameCorrectly() {
        final String testName = "Подвал Михалыча";
        Auditorium auditorium = auditoriumService.getByName(testName);
        assertThat(auditorium.getName(), is(testName));
    }
}
