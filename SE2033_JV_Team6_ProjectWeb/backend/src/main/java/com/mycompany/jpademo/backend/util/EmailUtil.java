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

    public static String buildBanAccountTemplate(String name) {
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
                """.formatted(name);
    }

    public static String buildUnbanAccountTemplate(String name) {
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
                """.formatted(name);
    }

    public static String buildDoctorRejectedTemplate(String name) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #c62828;">Kết quả đăng ký bác sĩ</h2>

                    <p>Xin chào <b>%s</b>,</p>

                    <p>
                        Chúng tôi rất tiếc phải thông báo rằng hồ sơ đăng ký bác sĩ của bạn <b>không được phê duyệt</b>.
                    </p>

                    <p>
                        Bạn có thể cập nhật thông tin và đăng ký lại trong tương lai.
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
}
