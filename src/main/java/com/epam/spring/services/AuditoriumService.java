package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;

import java.util.List;

public interface AuditoriumService {
    List<Auditorium> getAll();
    Auditorium getByName(String name);
}
