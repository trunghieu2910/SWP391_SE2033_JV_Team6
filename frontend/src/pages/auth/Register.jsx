import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../../services/authService';
import { Activity, User, Phone, MapPin, Calendar, CreditCard, Shield, Mail, Lock, Check, AlertCircle, Eye, EyeOff } from 'lucide-react';
import './Login.css';

export default function Register() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1: Personal Info, 2: Account details, 3: OTP Verification
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Password visibility states
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Form states
  const [fullName, setFullName] = useState('');
  const [gender, setGender] = useState('nam');
  const [dob, setDob] = useState('');
  const [address, setAddress] = useState('');
  const [nationalID, setNationalID] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [otp, setOtp] = useState('');

  const nextStep = (e) => {
    e.preventDefault();
    if (step === 1) {
      if (!fullName || !dob || !address || !nationalID || !phoneNumber) {
        setError('Vui lòng điền đầy đủ thông tin cá nhân.');
        return;
      }

      // Validate national ID format
      const cccdRegex = /^\d{12}$/;
      if (!cccdRegex.test(nationalID)) {
        setError('Số CMND/CCCD không hợp lệ. Vui lòng nhập đúng 12 chữ số.');
        return;
      }

      // Validate phone number format
      const phoneRegex = /^(0[35789]\d{8})$/;
      if (!phoneRegex.test(phoneNumber)) {
        setError('Số điện thoại không hợp lệ. Vui lòng nhập đúng 10 chữ số bắt đầu bằng 03, 05, 07, 08 hoặc 09.');
        return;
      }

      setError(null);
      setStep(2);
    }
  };

  const prevStep = () => {
    setError(null);
    setStep(1);
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    if (!userName || !email || !password || !confirmPassword) {
      setError('Vui lòng nhập đầy đủ thông tin tài khoản.');
      return;
    }

    // Check password rules: >= 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    if (password.length < 8) {
      setError('Mật khẩu phải từ 8 ký tự trở lên.');
      return;
    }
    if (!/[A-Z]/.test(password)) {
      setError('Mật khẩu phải chứa ít nhất 1 chữ cái in hoa.');
      return;
    }
    if (!/[a-z]/.test(password)) {
      setError('Mật khẩu phải chứa ít nhất 1 chữ cái in thường.');
      return;
    }
    if (!/[0-9]/.test(password)) {
      setError('Mật khẩu phải chứa ít nhất 1 chữ số.');
      return;
    }
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
      setError('Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (ví dụ: @, #, $, ...).');
      return;
    }

    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không trùng khớp.');
      return;
    }

    setError(null);
    setLoading(true);

    const registerData = {
      fullName,
      gender,
      dob,
      address,
      nationalID,
      phoneNumber,
      userName,
      password,
      email
    };

    try {
      const response = await authService.register(registerData);
      setSuccess(response.data.message || 'Đăng ký thành công. Vui lòng kiểm tra email để nhận mã OTP.');
      setStep(3);
    } catch (err) {
      console.error(err);
      if (err.response && err.response.data) {
        const data = err.response.data;
        if (data.message) {
          setError(data.message);
        } else if (typeof data === 'object') {
          // Parse Spring Boot validation map e.g. { "userName": "Username already exists", "email": "Email already exists" }
          const errorMsgs = Object.values(data).filter(val => typeof val === 'string');
          if (errorMsgs.length > 0) {
            setError(errorMsgs.join(' | '));
          } else {
            setError('Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.');
          }
        } else {
          setError('Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.');
        }
      } else {
        setError('Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleOtpVerify = async (e) => {
    e.preventDefault();
    if (!otp.trim()) {
      setError('Vui lòng nhập mã OTP.');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      await authService.verifyRegistrationOtp({ userName, otp });
      setSuccess('Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay.');
      setTimeout(() => {
        navigate('/login');
      }, 2500);
    } catch (err) {
      console.error(err);
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Mã OTP không đúng hoặc đã hết hạn.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setError(null);
    setLoading(true);
    try {
      const response = await authService.resendRegistrationOtp({ userName });
      setSuccess(response.data.message || 'OTP mới đã được gửi lại vào email của bạn.');
    } catch (err) {
      console.error(err);
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Gửi lại OTP không thành công.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-left">
        <div className="login-left-content">
          <div className="login-logo">
            <span>+</span>
          </div>
          <h1 className="login-app-name">MedAI</h1>
          <p className="login-tagline">Hệ thống sàng lọc ung thư cổ tử cung hỗ trợ bởi trí tuệ nhân tạo</p>
          <div className="login-decor">
            <div className="decor-circle c1" />
            <div className="decor-circle c2" />
            <div className="decor-circle c3" />
          </div>
        </div>
      </div>

      <div className="login-right">
        <div className="login-card card" style={{ width: '100%', maxWidth: '520px' }}>
          <div className="login-card-header">
            <h2>{step === 3 ? 'Kích hoạt tài khoản' : 'Đăng ký tài khoản bệnh nhân'}</h2>
            <p className="text-muted" style={{ fontSize: '0.9rem', marginTop: 4 }}>
              {step === 3 ? 'Nhập mã OTP đã gửi tới email để kích hoạt tài khoản.' : 'Điền thông tin để tạo tài khoản bệnh nhân mới.'}
            </p>
          </div>

          {error && (
            <div className="alert alert-error mb-16">
              <span>!</span>
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="alert alert-success mb-16">
              <span>✓</span>
              <span>{success}</span>
            </div>
          )}

          {step === 1 && (
            <form onSubmit={nextStep}>
              <div className="form-group">
                <label className="form-label" htmlFor="fullName">Họ và tên</label>
                <div style={{ position: 'relative' }}>
                  <input
                    id="fullName"
                    type="text"
                    className="form-control"
                    placeholder="Nguyễn Thị Hoa"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    style={{ paddingLeft: '40px' }}
                    required
                  />
                  <User size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label className="form-label">Giới tính</label>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <label className={`custom-control ${gender === 'nam' ? 'checked' : ''}`} style={{ flex: 1 }}>
                      <input type="radio" name="gender" value="nam" checked={gender === 'nam'} onChange={() => setGender('nam')} />
                      Nam
                    </label>
                    <label className={`custom-control ${gender === 'nu' ? 'checked' : ''}`} style={{ flex: 1 }}>
                      <input type="radio" name="gender" value="nu" checked={gender === 'nu'} onChange={() => setGender('nu')} />
                      Nữ
                    </label>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="dob">Ngày sinh</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      id="dob"
                      type="date"
                      className="form-control"
                      value={dob}
                      onChange={(e) => setDob(e.target.value)}
                      style={{ paddingLeft: '40px' }}
                      required
                    />
                    <Calendar size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="address">Địa chỉ</label>
                <div style={{ position: 'relative' }}>
                  <input
                    id="address"
                    type="text"
                    className="form-control"
                    placeholder="Quận Cầu Giấy, Hà Nội"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    style={{ paddingLeft: '40px' }}
                    required
                  />
                  <MapPin size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="nationalID">Số CCCD / CMND</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      id="nationalID"
                      type="text"
                      className="form-control"
                      placeholder="001305012345"
                      value={nationalID}
                      onChange={(e) => setNationalID(e.target.value)}
                      style={{ paddingLeft: '40px' }}
                      required
                    />
                    <CreditCard size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="phoneNumber">Số điện thoại</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      id="phoneNumber"
                      type="tel"
                      className="form-control"
                      placeholder="0912345678"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                      style={{ paddingLeft: '40px' }}
                      required
                    />
                    <Phone size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  </div>
                </div>
              </div>

              <button type="submit" className="btn btn-primary btn-full mt-24" disabled={loading}>
                Tiếp tục
              </button>
            </form>
          )}

          {step === 2 && (
            <form onSubmit={handleRegisterSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="userName">Tên tài khoản (Username)</label>
                <div style={{ position: 'relative' }}>
                  <input
                    id="userName"
                    type="text"
                    className="form-control"
                    placeholder="hoa_nguyen"
                    value={userName}
                    onChange={(e) => setUserName(e.target.value)}
                    style={{ paddingLeft: '40px' }}
                    required
                    disabled={loading}
                  />
                  <Shield size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">Email</label>
                <div style={{ position: 'relative' }}>
                  <input
                    id="email"
                    type="email"
                    className="form-control"
                    placeholder="hoa@gmail.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={{ paddingLeft: '40px' }}
                    required
                    disabled={loading}
                  />
                  <Mail size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="password">Mật khẩu</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      id="password"
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      style={{ paddingLeft: '40px', paddingRight: '40px' }}
                      required
                      disabled={loading}
                    />
                    <Lock size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="input-action"
                      tabIndex={-1}
                      style={{ right: '12px', top: '50%', transform: 'translateY(-50%)' }}
                      disabled={loading}
                    >
                      {showPassword ? 'Ẩn' : 'Hiện'}
                    </button>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">Nhập lại mật khẩu</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      id="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      className="form-control"
                      placeholder="••••••••"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      style={{ paddingLeft: '40px', paddingRight: '40px' }}
                      required
                      disabled={loading}
                    />
                    <Lock size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="input-action"
                      tabIndex={-1}
                      style={{ right: '12px', top: '50%', transform: 'translateY(-50%)' }}
                      disabled={loading}
                    >
                      {showConfirmPassword ? 'Ẩn' : 'Hiện'}
                    </button>
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
                <button type="button" onClick={prevStep} className="btn btn-secondary" style={{ flex: 1, padding: '12px' }} disabled={loading}>
                  Quay lại
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1, padding: '12px' }} disabled={loading}>
                  {loading ? 'Đang đăng ký...' : 'Đăng ký'}
                </button>
              </div>
            </form>
          )}

          {step === 3 && (
            <form onSubmit={handleOtpVerify}>
              <div style={{ textAlign: 'center', marginBottom: '24px' }}>
                <p style={{ fontSize: '0.95rem', color: 'var(--text-muted)' }}>
                  Một mã xác thực OTP gồm 6 chữ số đã được gửi tới địa chỉ email <strong>{email}</strong>. Vui lòng nhập mã để kích hoạt tài khoản.
                </p>
              </div>

              <div className="form-group" style={{ maxWidth: '280px', margin: '0 auto 24px auto' }}>
                <label className="form-label" style={{ textAlign: 'center' }} htmlFor="otp">Nhập mã OTP</label>
                <input
                  id="otp"
                  type="text"
                  className="form-control"
                  placeholder="123456"
                  maxLength={6}
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  style={{ textAlign: 'center', fontSize: '1.5rem', letterSpacing: '8px', padding: '10px' }}
                  required
                  disabled={loading}
                />
              </div>

              <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                {loading ? 'Đang xác thực...' : 'Xác thực tài khoản'}
              </button>

              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '24px', fontSize: '0.85rem' }}>
                <button type="button" onClick={handleResendOtp} style={{ background: 'none', border: 'none', color: 'hsl(var(--primary))', fontWeight: '500', cursor: 'pointer' }} disabled={loading}>
                  Gửi lại mã OTP
                </button>
                <Link to="/login" style={{ fontWeight: '500' }}>
                  Quay lại Đăng nhập
                </Link>
              </div>
            </form>
          )}

          {step < 3 && (
            <div style={{ marginTop: '24px', fontSize: '0.9rem', color: 'var(--text-muted)', textAlign: 'center' }}>
              Đã có tài khoản?{' '}
              <Link to="/login" style={{ fontWeight: '600', color: 'hsl(var(--primary))' }}>
                Đăng nhập ngay
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
