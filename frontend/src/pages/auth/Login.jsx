import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import authService from '../../services/authService';
import { LogIn, ShieldAlert } from 'lucide-react';

export const Login = () => {
  const [loginInput, setLoginInput] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!loginInput || !password) {
      setError('Vui lòng điền đầy đủ tên đăng nhập/email và mật khẩu.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      console.log('🔐 Attempting login with:', { login: loginInput });

      // Gọi api login - URL: /api/auth/login
      const responseData = await authService.login({
        login: loginInput.trim(),
        password: password
      });

      console.log('✅ Login response:', responseData);

      // Lấy token từ response
      const token = responseData.accessToken;

      if (!token) {
        throw new Error('Không nhận được token từ server');
      }

      // Tạo user object từ response
      const user = {
        userId: responseData.userId,
        userName: responseData.username || loginInput,
        fullName: responseData.fullName || 'Admin User',
        email: responseData.email || '',
        roleName: responseData.role || 'ADMIN'
      };

      console.log('👤 User object:', user);
      console.log('🔑 Token:', token.substring(0, 50) + '...');

      // Kiểm tra role (nếu có)
      if (user.roleName !== 'ADMIN') {
        setError('Tài khoản của bạn không có quyền truy cập hệ thống quản trị.');
        setLoading(false);
        return;
      }

      // Lưu vào Context và LocalStorage
      loginUser(user, token);

      // Chuyển hướng sang Dashboard
      navigate('/admin/dashboard', { replace: true });

    } catch (err) {
      console.error('❌ Login error details:', {
        message: err.message,
        response: err.response?.data,
        status: err.response?.status,
        config: err.config
      });

      // Xử lý thông báo lỗi
      let errorMessage = 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin tài khoản.';

      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.response?.data) {
        errorMessage = typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data);
      } else if (err.message) {
        errorMessage = err.message;
      }

      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="d-flex align-items-center justify-content-center bg-light min-vh-100 p-3">
        <div className="card shadow border-0" style={{ width: '450px', borderRadius: '12px' }}>
          <div className="card-body p-5">
            <div className="text-center mb-4">
              <div className="d-inline-flex align-items-center justify-content-center bg-primary text-white rounded-circle p-3 mb-3" style={{ width: '60px', height: '60px', backgroundColor: '#100357' }}>
                <LogIn size={28} />
              </div>
              <h3 className="font-weight-bold" style={{ color: '#100357' }}>Đăng Nhập Admin</h3>
              <p className="text-muted">Hệ thống hỗ trợ chẩn đoán ung thư cổ tử cung</p>
            </div>

            {error && (
                <div className="alert alert-danger d-flex align-items-center gap-2" role="alert">
                  <ShieldAlert size={20} className="flex-shrink-0" />
                  <div>{error}</div>
                </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label font-weight-semibold">Tên đăng nhập hoặc Email</label>
                <input
                    type="text"
                    className="form-control form-control-lg"
                    placeholder="Nhập username hoặc email..."
                    value={loginInput}
                    onChange={(e) => setLoginInput(e.target.value)}
                    disabled={loading}
                    autoFocus
                />
              </div>

              <div className="mb-4">
                <label className="form-label font-weight-semibold">Mật khẩu</label>
                <input
                    type="password"
                    className="form-control form-control-lg"
                    placeholder="Nhập mật khẩu..."
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={loading}
                />
              </div>

              <button
                  type="submit"
                  className="btn btn-lg w-100 text-white font-weight-bold"
                  style={{ backgroundColor: '#100357', border: 'none' }}
                  disabled={loading}
              >
                {loading ? (
                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                ) : (
                    'Đăng Nhập'
                )}
              </button>
            </form>

            {/* Debug info */}
            <div className="mt-3 p-2 bg-light rounded small">
              <p className="mb-0 text-muted">API URL: http://localhost:8082/api/auth/login</p>
              <p className="mb-0 text-muted">Status: {loading ? 'Đang xử lý...' : 'Sẵn sàng'}</p>
            </div>
          </div>
        </div>
      </div>
  );
};

export default Login;