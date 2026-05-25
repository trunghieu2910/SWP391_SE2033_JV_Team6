package com.mycompany.jpademo.backend.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("Invalid reset token");
    }
}
