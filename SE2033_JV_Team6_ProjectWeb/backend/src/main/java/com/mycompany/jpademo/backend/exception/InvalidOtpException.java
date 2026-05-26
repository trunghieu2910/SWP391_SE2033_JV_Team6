package com.mycompany.jpademo.backend.exception;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException() {
        super("OTP không hợp lệ hoặc đã hết hạn");
    }

}
