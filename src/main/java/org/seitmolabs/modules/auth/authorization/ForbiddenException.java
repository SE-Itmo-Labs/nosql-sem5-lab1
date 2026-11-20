package org.seitmolabs.modules.auth.authorization;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("Недостаточно прав для выполнения данного действия");
    }
}
