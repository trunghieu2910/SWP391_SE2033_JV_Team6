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
                    <h2 style="color: #d32f2f;">
                        Đăng ký bác sĩ đã bị từ chối
                    </h2>
                    <p>Xin chào Bác sĩ <b>%s</b>,</p>
                    <p>
                        Yêu cầu đăng ký tài khoản bác sĩ của bạn đã bị quản trị viên từ chối.
                    </p>
                    <div style="margin-top: 20px; padding: 15px; background-color: #ffebee; border-radius: 5px;">
                        <strong>Lý do từ chối:</strong>
                        <p>Chứng chỉ bác sĩ của bạn chưa đạt chuẩn.</p>
                    </div>
                    <br>
                    <p>
                        Vui lòng kiểm tra lại thông tin đăng ký và gửi yêu cầu mới nếu cần.
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

    public static String buildOtpEmailTemplate(String name, String otp) {

        return """
                <html>
                <body style="
                        font-family: Arial, sans-serif;
                        background-color: #f4f6f9;
                        padding: 20px;
                ">
                
                <div style="
                        max-width: 600px;
                        margin: auto;
                        background: white;
                        border-radius: 10px;
                        padding: 30px;
                        text-align: center;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                ">
                
                    <h2 style="color: #1565c0;">
                        Xác thực OTP
                    </h2>
                
                    <p>
                        Xin chào <b>%s</b>,
                    </p>
                
                    <p>
                        Mã OTP của bạn để xác thực tài khoản là:
                    </p>
                
                    <div style="
                            font-size: 32px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            color: #2e7d32;
                            margin: 30px 0;
                    ">
                        %s
                    </div>
                
                    <p style="color: #c62828;">
                        Mã OTP sẽ hết hạn sau 2 phút.
                    </p>
                
                    <p>
                        Vui lòng không chia sẻ mã này với bất kỳ ai.
                    </p>
                
                    <br>
                
                    <p>
                        Trân trọng,<br>
                        <strong>
                            Hệ Thống Hỗ trợ
                            Chẩn đoán bệnh ung thư tử cung
                        </strong>
                    </p>
                
                </div>
                
                </body>
                </html>
                """.formatted(name, otp);
    }
}