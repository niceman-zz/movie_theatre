package com.epam.spring.services;

import com.epam.spring.domain.User;
import com.epam.spring.exceptions.MovieTheatreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class UserServiceImpl implements UserService {
    private static AtomicLong ID_SEQUENCE = new AtomicLong(1);
    private static Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        if (getByEmail(user.getEmail()) != null) {
            throw new MovieTheatreException("User with this email already exists: " + user.getEmail());
        }
        user.setId(ID_SEQUENCE.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new MovieTheatreException("Can't update unregistered user: " + user.getFullName());
        }
        users.replace(user.getId(), user);
    }

    @Override
    public boolean remove(User user) {
        return users.remove(user.getId()) != null;
    }

    @Override
    public User getById(long id) {
        return users.get(id);
    }

    @Override
    public User getByEmail(String email) {
        if (email == null) {
            return null;
        }

        for (User user : users.values()) {
            if (email.equals(user.getEmail())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public boolean isRegistered(User user) {
        return user.getId() != null && users.containsKey(user.getId());
    }
}
