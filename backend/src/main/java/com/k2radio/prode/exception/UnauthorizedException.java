// src/main/java/com/k2radio/prode/exception/UnauthorizedException.java
package com.k2radio.prode.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}