package com.example.exceptions;

public class KeycloakRegistrationFailedException extends RuntimeException {
    public KeycloakRegistrationFailedException(String message) {
        super(message);
    }
}
