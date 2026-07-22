package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.GoogleCompleteRequest;
import com.mycompany.jpademo.backend.dto.response.GoogleSessionResult;
import com.mycompany.jpademo.backend.entity.User;

public interface GoogleAuthService {

    /** Xác thực idToken, tra cứu user theo email, kiểm tra trạng thái tài khoản. */
    GoogleSessionResult resolveSession(String idToken);

    /** Tạo tài khoản Patient mới từ thông tin bổ sung sau lần đăng nhập Google đầu tiên. */
    User completeRegistration(GoogleCompleteRequest request);
}