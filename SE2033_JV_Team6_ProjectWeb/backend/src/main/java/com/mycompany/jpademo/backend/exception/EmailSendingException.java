package com.mycompany.jpademo.backend.exception;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException() {
        super("Không thể gửi email");    }
}
