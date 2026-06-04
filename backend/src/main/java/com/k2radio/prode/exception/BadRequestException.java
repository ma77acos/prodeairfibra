// src/main/java/com/k2radio/prode/exception/BadRequestException.java
package com.k2radio.prode.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}