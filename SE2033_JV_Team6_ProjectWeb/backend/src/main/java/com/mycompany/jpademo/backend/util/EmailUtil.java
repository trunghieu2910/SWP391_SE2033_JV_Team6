package com.mycompany.jpademo.backend.util;

public class EmailUtil {

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