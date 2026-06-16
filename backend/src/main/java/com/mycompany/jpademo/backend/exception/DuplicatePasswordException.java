package com.mycompany.jpademo.backend.exception;

public class DuplicatePasswordException extends RuntimeException {
    public DuplicatePasswordException() {
        super("Mật khẩu mới không được trùng với mật khẩu cũ");
    }
}
