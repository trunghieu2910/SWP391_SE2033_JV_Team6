package com.mycompany.jpademo.backend.exception;

public class UseResetTokenAgainException extends RuntimeException {
    public UseResetTokenAgainException() {
        super("Mã đặt lại đã được sử dụng hoặc không hợp lệ!");
    }
}
