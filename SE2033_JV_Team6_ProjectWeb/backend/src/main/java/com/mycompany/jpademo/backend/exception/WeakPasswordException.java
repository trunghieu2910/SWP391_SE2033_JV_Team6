package com.mycompany.jpademo.backend.exception;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException() {
        super(
                "Password must contain "
                        + "8 characters, "
                        + "1 uppercase, "
                        + "1 lowercase, "
                        + "1 number and "
                        + "1 special character"
        );
    }
}
