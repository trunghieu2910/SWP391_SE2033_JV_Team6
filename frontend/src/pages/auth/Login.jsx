import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import authService from '../../services/authService';
import googleAuthService from '../../services/googleAuthService';
import GoogleCompleteModal from '../../components/GoogleCompleteModal';
import { ToastContainer, useToast } from '../../components/Toast';
import './Login.css';

const ROLE_HOME = {
    DOCTOR:    '/doctor/dashboard',
    PATIENT:   '/patient/home',
    ADMIN:     '/admin/dashboard',
    AITRAINER: '/trainer/dashboard',
};

export default function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const { toasts, addToast, removeToast } = useToast();

    const [form,     setForm]     = useState({ login: '', password: '' });
    const [errors,   setErrors]   = useState({});
    const [apiError, setApiError] = useState(null);
    const [loading,  setLoading]  = useState(false);
    const [showPwd,  setShowPwd]  = useState(false);
    const [googleLoading, setGoogleLoading] = useState(false);
    const [googleProfile, setGoogleProfile] = useState(null);
    const [pendingIdToken, setPendingIdToken] = useState('');
    const [showGcm, setShowGcm] = useState(false);

    const validate = () => {
        const e = {};
        if (!form.login.trim())    e.login    = 'Vui lòng nhập tài khoản';
        if (!form.password.trim()) e.password = 'Vui lòng nhập mật khẩu';
        return e;
    };

    const handleChange = (e) => {
        if (form[e.target.name] === e.target.value) return;

        setForm(p => ({ ...p, [e.target.name]: e.target.value }));
        setErrors(p => ({ ...p, [e.target.name]: '' }));
        setApiError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const errs = validate();
        if (Object.keys(errs).length) { setErrors(errs); return; }

        setLoading(true);
        setApiError(null);
        try {
            const res = await authService.login({ login: form.login, password: form.password });
            const { accessToken, role, username, userId, email } = res.data;
            login({ fullName: username, role, userID: userId, email }, accessToken);
            navigate(ROLE_HOME[role] ?? '/');
        } catch (err) {
            const status  = err.response?.status;
            const message = err.response?.data?.message ?? '';

            if (status === 401 && message.toLowerCase().includes('tài khoản bị khoá')) {
                setApiError({ type: 'banned', msg: 'Tài khoản của bạn đã bị tạm ngưng. Vui lòng liên hệ với quản trị viên.' });
            } else {
                setApiError({ type: 'invalid', msg: 'Thông tin đăng nhập không chính xác. Vui lòng thử lại.' });
            }
        } finally {
            setLoading(false);
        }
    };

    const handleGoogleLogin = async () => {
        setGoogleLoading(true);
        setApiError(null);
        try {
            const idToken = await googleAuthService.getGoogleIdToken();
            const res = await googleAuthService.googleLogin(idToken);

            if (res.status === 202 && res.data.status === 'NEED_MORE_INFO') {
                setGoogleProfile({ email: res.data.email, fullName: res.data.fullName });
                setPendingIdToken(idToken);
                setShowGcm(true);
            } else {
                const { accessToken, role, username, userId, email } = res.data;
                login({ fullName: username, role, userID: userId, email }, accessToken);
                navigate(ROLE_HOME[role] ?? '/');
            }
        } catch (err) {
            if (err.code === 'auth/popup-closed-by-user') return;
            setApiError({ type: 'invalid', msg: 'Đăng nhập bằng Google thất bại. Vui lòng thử lại.' });
        } finally {
            setGoogleLoading(false);
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
                <div className="login-card card">
                    <div className="login-card-header">
                        <h2>Đăng nhập</h2>
                        <p className="text-muted" style={{ fontSize: '0.9rem', marginTop: 4 }}>
                            Nhập email, tên người dùng, số điện thoại hoặc số CCCD của bạn.
                        </p>
                    </div>

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="form-group">
                            <label className="form-label" htmlFor="login">
                                Tài khoản <span className="required">*</span>
                            </label>
                            <input
                                id="login" name="login"
                                className={`form-input ${errors.login ? 'error' : ''}`}
                                placeholder="Email, tên người dùng, số điện thoại, số CCCD"
                                value={form.login}
                                onChange={handleChange}
                                autoComplete="username"
                                autoFocus
                            />
                            {errors.login && <span className="form-error">! {errors.login}</span>}
                        </div>

                        <div className="form-group mt-16">
                            <label className="form-label" htmlFor="password">
                                Mật khẩu <span className="required">*</span>
                            </label>
                            <div className="input-wrapper">
                                <input
                                    id="password" name="password"
                                    type={showPwd ? 'text' : 'password'}
                                    className={`form-input ${errors.password ? 'error' : ''}`}
                                    placeholder="Nhập mật khẩu của bạn"
                                    value={form.password}
                                    onChange={handleChange}
                                    autoComplete="current-password"
                                />
                                <button type="button" className="input-action" onClick={() => setShowPwd(p => !p)} tabIndex={-1}>
                                    {showPwd ? 'Ẩn' : 'Hiện'}
                                </button>
                            </div>
                            {errors.password && <span className="form-error">! {errors.password}</span>}
                        </div>

                        <div className="login-action-row">
                            <Link to="/forgot-password" className="login-forgot-link">
                                Bạn quên mật khẩu?
                            </Link>
                            <Link to="/register" className="login-register-link">
                                Đăng ký
                            </Link>
                        </div>

                        <button type="submit" className="btn btn-primary btn-full mt-24" disabled={loading}>
                            {loading ? <span className="spinner" /> : null}
                            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
                        </button>
                    </form>

                    {showGcm && (
                        <GoogleCompleteModal
                            googleProfile={googleProfile}
                            idToken={pendingIdToken}
                            onSuccess={(userData, token) => {
                                login(userData, token);
                                addToast('Vui lòng kiểm tra email để nhận mật khẩu tự động.', 'success');
                                setShowGcm(false);
                                setPendingIdToken('');
                                setGoogleProfile(null);
                                setTimeout(() => {
                                    navigate(ROLE_HOME[userData.role] ?? '/');
                                }, 1200);
                            }}
                            onCancel={() => {
                                setShowGcm(false);
                                setPendingIdToken('');
                                setGoogleProfile(null);
                            }}
                        />
                    )}

                    <div className="login-divider">
                        <span>hoặc</span>
                    </div>

                    <button
                        type="button"
                        className="btn btn-google btn-full"
                        onClick={handleGoogleLogin}
                        disabled={googleLoading || loading}
                    >
                        {googleLoading
                            ? <span className="spinner spinner-dark" />
                            : <img
                                src="https://www.svgrepo.com/show/475656/google-color.svg"
                                alt="Google"
                                width={20}
                                height={20}
                            />
                        }
                        {googleLoading ? 'Đang kết nối...' : 'Đăng nhập với Google'}
                    </button>

                    <br />
                    <br />
                    {apiError && (
                        <div className="alert alert-error">
                            <span>{apiError.type === 'banned' ? '!' : ''}</span>
                            <span>{apiError.msg}</span>
                        </div>
                    )}
                </div>
            </div>
            <ToastContainer toasts={toasts} onRemove={removeToast} />
        </div>
    );
}
