import { useState, useEffect, useRef, useCallback } from 'react';
import { Link } from 'react-router-dom';
import authService from '../../services/authService';
import './Login.css';
import './ForgotPassword.css';

const STEPS = ['Gửi OTP', 'Xác minh OTP', 'Đặt mật khẩu mới'];
const OTP_TTL = 120; // seconds

/* ── Password rule checker ── */
const RULES = [
  { id: 'len',     label: 'Ít nhất 8 ký tự',      test: v => v.length >= 8 },
  { id: 'upper',   label: 'Ít nhât một chữ cái viết hoa (A–Z)',  test: v => /[A-Z]/.test(v) },
  { id: 'lower',   label: 'Ít nhất một chữ cái viết thường (a–z)',  test: v => /[a-z]/.test(v) },
  { id: 'digit',   label: 'Ít nhất một chữ số (0–9)',            test: v => /\d/.test(v) },
  { id: 'special', label: 'Ít nhất một ký tự đặc biệt (!@#$)',test: v => /[^A-Za-z0-9]/.test(v) },
];

function StepIndicator({ current }) {
  return (
    <div className="step-indicator">
      {STEPS.map((label, i) => (
        <div key={i} className="step-item">
          <div className={`step-circle ${i < current ? 'done' : i === current ? 'active' : ''}`}>
            {i < current ? '✓' : i + 1}
          </div>
          <span className={`step-label ${i === current ? 'active' : ''}`}>{label}</span>
          {i < STEPS.length - 1 && <div className={`step-line ${i < current ? 'done' : ''}`} />}
        </div>
      ))}
    </div>
  );
}

export default function ForgotPassword() {
  const [step, setStep] = useState(0);

  // Step 1
  const [email, setEmail]         = useState('');
  const [emailErr, setEmailErr]   = useState('');
  const [step1Err, setStep1Err]   = useState('');
  const [step1Load, setStep1Load] = useState(false);

  // Step 2
  const [otp, setOtp]             = useState(['','','','','','']);
  const [otpErr, setOtpErr]       = useState('');
  const [timer, setTimer]         = useState(OTP_TTL);
  const [canResend, setCanResend] = useState(false);
  const [step2Load, setStep2Load] = useState(false);
  const otpRefs = useRef([]);

  // Step 3
  const [newPwd, setNewPwd]       = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [showNew, setShowNew]     = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [pwdErr, setPwdErr]       = useState('');
  const [step3Err, setStep3Err]   = useState('');
  const [step3Load, setStep3Load] = useState(false);
  const [success, setSuccess]     = useState(false);

  const [resetToken, setResetToken] = useState('');

  // OTP timer
  useEffect(() => {
    if (step !== 1) return;
    setTimer(OTP_TTL); setCanResend(false);
    const iv = setInterval(() => {
      setTimer(t => {
        if (t <= 1) { clearInterval(iv); setCanResend(true); return 0; }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(iv);
  }, [step]);

  const formatTime = s => `${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`;

  /* ── Step 1: Send OTP ── */
  const handleSendOtp = async (e) => {
    e.preventDefault();
    setEmailErr(''); setStep1Err('');
    if (!email.trim()) { setEmailErr('Cần có email'); return; }
    if (!/\S+@\S+\.\S+/.test(email)) { setEmailErr('Vui lòng nhập địa chỉ email hợp lệ'); return; }
    setStep1Load(true);
    try {
      await authService.forgotPassword({ email });
      setStep(1);
    } catch (err) {
      const status = err.response?.status;
      if (status === 404) setStep1Err('Không tìm thấy tài khoản nào với email này.');
      else if (status === 400) setEmailErr('Vui lòng nhập địa chỉ email hợp lệ.');
      else setStep1Err('Đã xảy ra lỗi. Vui lòng thử lại.');
    } finally { setStep1Load(false); }
  };

  /* ── Step 2: Verify OTP ── */
  const handleOtpInput = (i, val) => {
    if (!/^\d?$/.test(val)) return;
    const next = [...otp]; next[i] = val;
    setOtp(next); setOtpErr('');
    if (val && i < 5) otpRefs.current[i+1]?.focus();
  };
  const handleOtpKey = (i, e) => {
    if (e.key === 'Backspace' && !otp[i] && i > 0) otpRefs.current[i-1]?.focus();
  };
  const handleOtpPaste = (e) => {
    e.preventDefault();
    const digits = e.clipboardData.getData('text').replace(/\D/g,'').slice(0,6).split('');
    const next = [...otp];
    digits.forEach((d, i) => { next[i] = d; });
    setOtp(next);
    otpRefs.current[Math.min(digits.length, 5)]?.focus();
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    const code = otp.join('');
    setOtpErr('');
    if (code.length < 6) { setOtpErr('Vui lòng nhập đầy đủ 6 chữ số'); return; }
    setStep2Load(true);
    try {
      const res = await authService.verifyOtp({ email, otp: code });
      setResetToken(res.data.resetToken);
      setStep(2);
    } catch (err) {
      const msg = err.response?.data?.message ?? '';
      if (msg.toLowerCase().includes('expir')) setOtpErr('Mã OTP này đã hết hạn. Vui lòng yêu cầu mã mới.');
      else setOtpErr('Mã OTP không chính xác. Vui lòng thử lại.');
    } finally { setStep2Load(false); }
  };

  const handleResend = useCallback(async () => {
    if (!canResend) return;
    await authService.forgotPassword({ email });
    setOtp(['','','','','','']);
    setCanResend(false); setTimer(OTP_TTL); setOtpErr('');
  }, [canResend, email]);

  /* ── Step 3: Reset Password ── */
  const rulesPassed = RULES.map(r => r.test(newPwd));

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setPwdErr(''); setStep3Err('');
    if (!rulesPassed.every(Boolean)) { setPwdErr('Mật khẩu không đáp ứng đầy đủ các yêu cầu.'); return; }
    if (newPwd !== confirmPwd) { setPwdErr('Mật khẩu không khớp.'); return; }
    setStep3Load(true);
    try {
      await authService.resetPassword({ resetToken, newPassword: newPwd });
      setSuccess(true);
    } catch (err) {
      const msg = err.response?.data?.message ?? '';
      if (msg.toLowerCase().includes('trùng')) {
        setStep3Err('Mật khẩu mới phải khác với mật khẩu hiện tại của bạn.');
      } else if (err.response?.status === 400) {
        setStep3Err('Phiên làm việc đã hết hạn hoặc không hợp lệ. Vui lòng bắt đầu lại.');
      } else {
        setStep3Err('Đã xảy ra lỗi. Vui lòng thử lại.');
      }
    } finally { setStep3Load(false); }
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
        <div className="login-card card">
          <div className="login-card-header">
            <h2>Quên mật khẩu</h2>
            <p className="text-muted" style={{ fontSize: '0.9rem', marginTop: 4 }}>
              Nhập email đã đăng ký để nhận mã OTP đặt lại mật khẩu.
            </p>
          </div>

          {step1Err && <div className="alert alert-error mb-16"><span>❌</span>{step1Err}</div>}
          {otpErr && <div className="alert alert-error mb-16"><span>❌</span>{otpErr}</div>}
          {step3Err && <div className="alert alert-error mb-16"><span>❌</span>{step3Err}</div>}
          {success && <div className="alert alert-success mb-16"><span>✓</span> Mật khẩu đã được đặt lại thành công.</div>}

          {step === 0 && (
            <>
              <form onSubmit={handleSendOtp} noValidate>
                <div className="form-group">
                  <label className="form-label" htmlFor="fp-email">Email <span className="required">*</span></label>
                  <input
                    id="fp-email" type="email"
                    className={`form-input ${emailErr ? 'error' : ''}`}
                    placeholder="doctor@hospital.com"
                    value={email} onChange={e => { setEmail(e.target.value); setEmailErr(''); }}
                    autoFocus
                  />
                  {emailErr && <span className="form-error">⚠ {emailErr}</span>}
                </div>
                <button className="btn btn-primary btn-full mt-24" type="submit" disabled={step1Load}>
                  {step1Load ? <span className="spinner" /> : null}
                  {step1Load ? 'Đang gửi…' : 'Gửi OTP'}
                </button>
                <div className="login-forgot-row" style={{ justifyContent: 'center', marginTop: '18px' }}>
                  <Link to="/login" className="login-forgot-link">← Quay lại trang đăng nhập</Link>
                </div>
              </form>
            </>
          )}

          {step === 1 && (
            <>
              <div className="login-card-header">
                <h2>Xác minh OTP</h2>
                <p className="text-muted" style={{ fontSize: '0.9rem', marginTop: 4 }}>
                  Mã gồm 6 chữ số đã được gửi tới <strong>{email}</strong>. Hãy kiểm tra hộp thư đến.
                </p>
              </div>
              <form onSubmit={handleVerifyOtp} noValidate>
                <div className="otp-boxes" onPaste={handleOtpPaste}>
                  {otp.map((d, i) => (
                    <input
                      key={i}
                      ref={el => otpRefs.current[i] = el}
                      type="text" inputMode="numeric" maxLength={1}
                      className={`otp-input ${otpErr ? 'error' : ''}`}
                      value={d}
                      onChange={e => handleOtpInput(i, e.target.value)}
                      onKeyDown={e => handleOtpKey(i, e)}
                    />
                  ))}
                </div>
                <div className={`otp-timer ${timer === 0 ? 'expired' : ''}`}>
                  {timer > 0
                    ? <>⏱ Mã hết hạn sau <strong>{formatTime(timer)}</strong></>
                    : '⚠️ OTP đã hết hạn.'}
                </div>
                <div className="otp-resend-row">
                  <button
                    type="button"
                    className={`otp-resend-btn ${canResend ? 'active' : ''}`}
                    onClick={handleResend} disabled={!canResend}
                  >
                    Gửi lại OTP
                  </button>
                </div>
                <button className="btn btn-primary btn-full mt-24" type="submit" disabled={step2Load}>
                  {step2Load ? <span className="spinner" /> : null}
                  {step2Load ? 'Đang xác minh…' : 'Xác minh mã'}
                </button>
                <div className="login-forgot-row" style={{ justifyContent: 'center', marginTop: '18px' }}>
                  <Link to="/login" className="login-forgot-link">← Quay lại trang đăng nhập</Link>
                </div>
              </form>
            </>
          )}

          {step === 2 && !success && (
            <>
              <div className="login-card-header">
                <h2>Đặt mật khẩu mới</h2>
                <p className="text-muted" style={{ fontSize: '0.9rem', marginTop: 4 }}>
                  Mật khẩu của bạn phải đáp ứng tất cả các yêu cầu dưới đây.
                </p>
              </div>
              {step3Err && (
                <div className="alert alert-error mb-16">
                  <span>❌</span>
                  <span>{step3Err}
                    {step3Err.includes('start over') && (
                      <button className="btn btn-sm btn-outline" style={{marginLeft:8}} onClick={() => setStep(0)}>Bắt đầu lại</button>
                    )}
                  </span>
                </div>
              )}
              <form onSubmit={handleResetPassword} noValidate>
                <div className="form-group">
                  <label className="form-label" htmlFor="new-pwd">Mật khẩu mới <span className="required">*</span></label>
                  <div className="input-wrapper">
                    <input id="new-pwd" type={showNew ? 'text' : 'password'}
                      className={`form-input ${pwdErr ? 'error' : ''}`}
                      placeholder="Nhập mật khẩu mới"
                      value={newPwd} onChange={e => setNewPwd(e.target.value)}
                      autoFocus
                    />
                    <button type="button" className="input-action" onClick={() => setShowNew(p=>!p)} tabIndex={-1}>
                      {showNew ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>
                <div className="form-group mt-16">
                  <label className="form-label" htmlFor="confirm-pwd">Xác nhận mật khẩu <span className="required">*</span></label>
                  <div className="input-wrapper">
                    <input id="confirm-pwd" type={showConfirm ? 'text' : 'password'}
                      className={`form-input ${pwdErr && newPwd !== confirmPwd ? 'error' : ''}`}
                      placeholder="Nhập lại mật khẩu mới"
                      value={confirmPwd} onChange={e => setConfirmPwd(e.target.value)}
                    />
                    <button type="button" className="input-action" onClick={() => setShowConfirm(p=>!p)} tabIndex={-1}>
                      {showConfirm ? '🙈' : '👁️'}
                    </button>
                  </div>
                  {pwdErr && <span className="form-error">⚠ {pwdErr}</span>}
                </div>
                {/* Password strength checklist */}
                <ul className="pwd-rules mt-16">
                  {RULES.map((r, i) => (
                    <li key={r.id} className={rulesPassed[i] ? 'pass' : newPwd ? 'fail' : ''}>
                      <span className="rule-icon">{rulesPassed[i] ? '✓' : '○'}</span>
                      {r.label}
                    </li>
                  ))}
                </ul>
                <button className="btn btn-primary btn-full mt-24" type="submit" disabled={step3Load}>
                  {step3Load ? <span className="spinner" /> : null}
                  {step3Load ? 'Đang đặt lại…' : 'Đặt lại mật khẩu'}
                </button>
              </form>
            </>
          )}

          {success && (
            <div className="alert alert-success mb-16">
              <span>✓</span>
              <span>Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập lại ngay.</span>
            </div>
          )}
          {success && (
            <Link to="/login" className="btn btn-outline btn-full mt-24" style={{ textDecoration: 'none', display: 'inline-flex', justifyContent: 'center' }}>
              Quay lại trang đăng nhập
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
