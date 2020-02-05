package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ContextConfiguration("classpath: spring.xml")
public class AuditoriumServiceTest {
    @Autowired
    private AuditoriumService auditoriumService;

    @Test
    public void shouldGetAllAuditoriums() {
        List<Auditorium> auditoriums = auditoriumService.getAll();
        assertThat(auditoriums.size(), is(4));
    }
}
