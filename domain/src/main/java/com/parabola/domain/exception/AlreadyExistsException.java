package com.parabola.domain.exception;

public class AlreadyExistsException extends IllegalArgumentException {

    public AlreadyExistsException() {
    }

    public AlreadyExistsException(String s) {
        super(s);
    }

    public AlreadyExistsException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
