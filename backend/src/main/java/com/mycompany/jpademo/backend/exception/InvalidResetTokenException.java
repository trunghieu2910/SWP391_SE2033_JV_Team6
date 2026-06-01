package com.mycompany.jpademo.backend.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("Mã đặt lại không hợp lệ");
    }
}
