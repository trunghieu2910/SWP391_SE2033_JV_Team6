package com.mycompany.jpademo.backend.util;

public class EmailUtil {
    private EmailUtil() {}

    public static String buildDoctorApprovedTemplate(String name) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #2e7d32;">
                        Đăng ký bác sĩ đã được phê duyệt
                    </h2>
                    <p>Xin chào Bác sĩ <b>%s</b>,</p>
                    <p>
                        Tài khoản bác sĩ của bạn đã được quản trị viên phê duyệt thành công.
                    </p>
                    <p>
                        Bạn hiện có thể đăng nhập và sử dụng Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung.
                    </p>
                    <br>
                    <p>
                        Trân trọng,<br>
                        <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                    </p>
                </div>
                </body>
                </html>
                """.formatted(name);
    }

    public static String buildBanAccountTemplate(String name, String reason) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #c62828;">
                    Tài khoản đã bị khóa
                </h2>
                <p>Xin chào <b>%s</b></p>
                <p>
                    Tài khoản của bạn đã bị khóa bởi quản trị viên hệ thống.
                </p>
                <p>
                    <strong>Lý do:</strong> %s
                </p>
                <br>
                <p>
                    Nếu bạn cho rằng đây là nhầm lẫn,
                    vui lòng liên hệ quản trị viên hoặc bộ phận hỗ trợ.
                </p>
                <br>
                <p>
                    Trân trọng,<br>
                    <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                </p>
            </div>
            </body>
            </html>
            """.formatted(name, reason);
    }

    public static String buildUnbanAccountTemplate(String name, String reason) {
        return  """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #1565c0;">
                    Tài khoản đã được mở khóa
                </h2>
                <p>Xin chào <b>%s</b></p>
                <p>
                    Tài khoản của bạn đã được quản trị viên mở khóa thành công.
                </p>
                <p>
                    <strong>Lý do:</strong> %s
                </p>
                <p>
                    Bạn hiện có thể đăng nhập và tiếp tục sử dụng Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung.
                </p>
                <br>
                <p>
                    Cảm ơn bạn đã hợp tác với hệ thống.
                </p>
                <br>
                <p>
                    Trân trọng,<br>
                    <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                </p>
            </div>
            </body>
            </html>
            """.formatted(name, reason);
    }

    public static String buildInactiveAccountTemplate(String name, String reason) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #ff9800;">
                    Tài khoản đã được chuyển sang trạng thái không hoạt động
                </h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>
                    Tài khoản của bạn đã được quản trị viên chuyển sang trạng thái <strong>KHÔNG HOẠT ĐỘNG</strong>.
                </p>
                <div style="margin-top: 20px; padding: 15px; background-color: #fff3e0; border-radius: 5px;">
                    <strong>Lý do:</strong>
                    <p>%s</p>
                </div>
                <br>
                <p>
                    Ở trạng thái này, bạn sẽ không thể đăng nhập và sử dụng hệ thống.
                </p>
                <p>
                    Nếu có thắc mắc, vui lòng liên hệ quản trị viên để được hỗ trợ.
                </p>
                <br>
                <p>
                    Trân trọng,<br>
                    <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                </p>
            </div>
            </body>
            </html>
            """.formatted(name, reason);
    }

    public static String buildOtpEmailTemplate(String name, String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #1565c0;">
                        Mã OTP đặt lại mật khẩu
                    </h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>
                        Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã OTP dưới đây:
                    </p>
                    <div style="margin-top: 20px; padding: 15px; background-color: #e3f2fd; border-radius: 5px; text-align: center;">
                        <h1 style="color: #1565c0; letter-spacing: 8px;">%s</h1>
                    </div>
                    <p>Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>
                    <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
                    <br>
                    <p>
                        Trân trọng,<br>
                        <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                    </p>
                </div>
                </body>
                </html>
                """.formatted(name, otp);
    }

    public static String buildCreateDoctorAccountTemplate(String name, String username, String password) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #1565c0;">
                        Tài khoản bác sĩ đã được tạo
                    </h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>
                        Tài khoản bác sĩ của bạn đã được tạo thành công.
                    </p>
                    <div style="margin-top: 20px; padding: 15px; background-color: #e8f5e9; border-radius: 5px;">
                        <strong>Thông tin tài khoản:</strong>
                        <p><strong>Tên đăng nhập:</strong> %s</p>
                        <p><strong>Mật khẩu:</strong> %s</p>
                    </div>
                    <br>
                    <p>
                        Vui lòng đăng nhập và cập nhật thông tin cá nhân của bạn.
                    </p>
                    <br>
                    <p>
                        Trân trọng,<br>
                        <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                    </p>
                </div>
                </body>
                </html>
                """.formatted(name, username, password);
    }

    public static String buildCreateDoctorOtpForAdmin(String adminName, String otp) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #1565c0;">Xác nhận tạo tài khoản bác sĩ</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>
                    Bạn vừa yêu cầu tạo tài khoản Bác sĩ.
                </p>
                <p>
                    Vui lòng sử dụng mã OTP bên dưới để xác nhận thao tác này.
                </p>
                <div style="margin-top: 20px; padding: 15px; background-color: #e3f2fd; border-radius: 5px; text-align: center;">
                    <h1 style="color: #1565c0; letter-spacing: 6px; margin: 0;">%s</h1>
                </div>
                <p style="margin-top: 12px;">
                    Mã OTP có hiệu lực trong <strong>10 phút</strong>. Không chia sẻ mã này với người khác.
                </p>
                <br>
                <p>
                    Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ bộ phận quản trị.
                </p>
                <br>
                <p>
                    Trân trọng,<br>
                    <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                </p>
            </div>
            </body>
            </html>
            """.formatted(adminName, otp);
    }
    public static String buildPasswordEmailTemplate(String fullName, String rawPassword) {
        return """
                <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 8px rgba(0,0,0,0.05);">
                
                        <h2 style="color: #1565c0; border-bottom: 2px solid #e3f2fd; padding-bottom: 10px;">
                            Thông tin tài khoản của bạn
                        </h2>
                
                        <p>Xin chào <b>%s</b>,</p>
                
                        <p>Tài khoản của bạn đã được tạo thành công thông qua Google. Đây là thông tin đăng nhập của bạn:</p>
                
                        <div style="margin: 20px 0; padding: 20px; background-color: #e3f2fd; border-radius: 5px; text-align: center;">
                            <p style="margin: 0; font-size: 14px; color: #1565c0;">Mật khẩu tạm thời của bạn là:</p>
                            <p style="font-size: 24px; font-weight: bold; font-family: monospace; color: #000; margin: 10px 0 0 0;">
                                %s
                            </p>
                        </div>
                
                        <p style="color: #d32f2f;">
                            <b>⚠️ Lưu ý:</b> Vui lòng đăng nhập và đổi mật khẩu ngay sau khi đăng nhập lần đầu để đảm bảo an toàn.
                        </p>
                
                        <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
                
                        <br>
                        <p>
                            Trân trọng,<br>
                            <strong>Hệ Thống Hỗ trợ Chẩn đoán bệnh ung thư tử cung</strong>
                        </p>
              
                    </div>
                    </body>
                    </html>
                """.formatted(fullName, rawPassword);
    }
}
