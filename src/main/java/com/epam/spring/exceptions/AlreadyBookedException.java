package com.epam.spring.exceptions;

public class AlreadyBookedException extends Exception {
    public AlreadyBookedException(String msg) {
        super(msg);
    }
}
