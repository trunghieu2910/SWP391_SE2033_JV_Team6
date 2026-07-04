package com.mycompany.jpademo.backend.exception;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException() {
        super(
                "Mật khẩu phải có ít nhất "
                        + "8 ký tự, "
                        + "1 chữ hoa, "
                        + "1 chữ thường, "
                        + "1 chữ số và "
                        + "1 ký tự đặc biệt"
        );
    }
}
