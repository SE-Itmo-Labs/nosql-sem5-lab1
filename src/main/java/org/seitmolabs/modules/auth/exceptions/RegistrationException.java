package org.seitmolabs.modules.auth.exceptions;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException() {
        super("RegistrationException");
    }
}