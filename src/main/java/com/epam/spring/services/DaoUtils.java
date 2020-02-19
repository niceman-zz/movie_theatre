package com.epam.spring.services;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class DaoUtils {
    public static <T> T getByField(String query, Object fieldValue, JdbcTemplate jdbcTemplate, RowMapper<T> rowMapper) {
        try {
            return jdbcTemplate.queryForObject(query, new Object[] {fieldValue}, rowMapper);
        } catch (EmptyResultDataAccessException stupidSpringDevelopers) {
            return null;
        }
    }

    public static <T> T getByField(String query, Object fieldValue, JdbcTemplate jdbcTemplate, Class<T> classObject) {
        try {
            return jdbcTemplate.queryForObject(query, new Object[] {fieldValue}, classObject);
        } catch (EmptyResultDataAccessException stupidSpringDevelopers) {
            return null;
        }
    }
}
