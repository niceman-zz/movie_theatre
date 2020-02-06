package com.epam.spring.services;

import com.epam.spring.domain.User;

import java.util.List;

public interface UserService {
    User save(User user);
    boolean remove(User user);
    User getById(long id);
    User getByEmail(String email);
    List<User> getAll();
    void clear();
}
