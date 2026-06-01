import { useState } from 'react';
import '../../styles/Register.css';

export default function Register() {
  const [step, setStep] = useState(1); // 1: Register form, 2: OTP verification
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [userName, setUserName] = useState('');

  // Form data
  const [formData, setFormData] = useState({
    fullName: '',
    gender: '',
    dob: '',
    address: '',
    nationalID: '',
    phoneNumber: '',
    userName: '',
    password: '',
    email: '',
  });

  const [otp, setOtp] = useState('');

  // Handle input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // Handle register submit
  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'Đăng ký thất bại');
        return;
      }

      setSuccess(data.message);
      setUserName(formData.userName);
      setStep(2); // Move to OTP verification
    } catch (err) {
      setError('Lỗi kết nối server: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Handle OTP verification
  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/verify-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userName,
          otp,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'Xác thực OTP thất bại');
        return;
      }

      setSuccess('✅ ' + data.message);
      setTimeout(() => {
        setStep(1);
        setFormData({
          fullName: '',
          gender: '',
          dob: '',
          address: '',
          nationalID: '',
          phoneNumber: '',
          userName: '',
          password: '',
          email: '',
        });
        setOtp('');
      }, 2000);
    } catch (err) {
      setError('Lỗi kết nối server: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Handle resend OTP
  const handleResendOtp = async () => {
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/resend-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userName }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'Gửi lại OTP thất bại');
        return;
      }

      setSuccess(data.message);
    } catch (err) {
      setError('Lỗi kết nối server: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        {step === 1 ? (
          <>
            <h2>Đăng Ký Tài Khoản</h2>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            <form onSubmit={handleRegister}>
              <div className="form-group">
                <label>Họ và tên:</label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleInputChange}
                  placeholder="Nguyễn Văn A"
                  required
                />
              </div>

              <div className="form-group">
                <label>Giới tính:</label>
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">-- Chọn giới tính --</option>
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
                  <option value="Khác">Khác</option>
                </select>
              </div>

              <div className="form-group">
                <label>Ngày sinh:</label>
                <input
                  type="date"
                  name="dob"
                  value={formData.dob}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Địa chỉ:</label>
                <input
                  type="text"
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  placeholder="123 Nguyễn Huy Tưởng, HN"
                  required
                />
              </div>

              <div className="form-group">
                <label>Số CMND/CCCD:</label>
                <input
                  type="text"
                  name="nationalID"
                  value={formData.nationalID}
                  onChange={handleInputChange}
                  placeholder="012345678901"
                  maxLength="12"
                  required
                />
              </div>

              <div className="form-group">
                <label>Số điện thoại:</label>
                <input
                  type="text"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleInputChange}
                  placeholder="0912345678"
                  maxLength="10"
                  required
                />
              </div>

              <div className="form-group">
                <label>Tên đăng nhập:</label>
                <input
                  type="text"
                  name="userName"
                  value={formData.userName}
                  onChange={handleInputChange}
                  placeholder="nguyenvana"
                  required
                />
              </div>

              <div className="form-group">
                <label>Mật khẩu:</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="MyPassword@123"
                  required
                />
                <small>Tối thiểu 8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt</small>
              </div>

              <div className="form-group">
                <label>Email:</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="user@gmail.com"
                  required
                />
              </div>

              <button type="submit" disabled={loading}>
                {loading ? 'Đang xử lý...' : 'Đăng Ký'}
              </button>
            </form>
          </>
        ) : (
          <>
            <h2>Xác Thực OTP</h2>
            <p>Mã OTP đã được gửi đến email của bạn. Vui lòng nhập mã OTP dưới đây.</p>

            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            <form onSubmit={handleVerifyOtp}>
              <div className="form-group">
                <label>Mã OTP (6 chữ số):</label>
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  placeholder="123456"
                  maxLength="6"
                  required
                />
              </div>

              <button type="submit" disabled={loading}>
                {loading ? 'Đang xử lý...' : 'Xác Thực OTP'}
              </button>

              <button
                type="button"
                onClick={handleResendOtp}
                disabled={loading}
                className="resend-btn"
              >
                Gửi Lại OTP
              </button>

              <button
                type="button"
                onClick={() => setStep(1)}
                className="back-btn"
              >
                Quay Lại
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}

