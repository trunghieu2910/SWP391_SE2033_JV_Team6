package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Kết quả nghiệp vụ của bước đăng nhập Google (trước khi thiết lập session HTTP).
 * Tách riêng để Service không phụ thuộc HttpServletRequest/Response.
 */
@Getter
@AllArgsConstructor
public class GoogleSessionResult {

    public enum Status { OK, NEED_MORE_INFO, BANNED, INACTIVE, LOCKED }

    private final Status status;
    private final User user;       // chỉ có giá trị khi status == OK
    private final String email;    // chỉ có giá trị khi status == NEED_MORE_INFO
    private final String fullName; // chỉ có giá trị khi status == NEED_MORE_INFO

    public static GoogleSessionResult ok(User user) {
        return new GoogleSessionResult(Status.OK, user, null, null);
    }

    public static GoogleSessionResult needMoreInfo(String email, String fullName) {
        return new GoogleSessionResult(Status.NEED_MORE_INFO, null, email, fullName);
    }

    public static GoogleSessionResult rejected(Status status) {
        return new GoogleSessionResult(status, null, null, null);
    }
}