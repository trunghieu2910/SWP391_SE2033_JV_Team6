import { useState } from 'react';
import googleAuthService from '../services/googleAuthService';
import './GoogleCompleteModal.css';

export default function GoogleCompleteModal({ googleProfile, idToken, onSuccess, onCancel }) {
  const [form, setForm] = useState({ userName: '', phoneNumber: '', nationalID: '' });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.userName || !/^\w{3,50}$/.test(form.userName)) {
      e.userName = 'Tên đăng nhập phải có 3-50 ký tự, chỉ gồm chữ, số hoặc dấu gạch dưới.';
    }
    if (!form.phoneNumber || !/^0\d{9}$/.test(form.phoneNumber)) {
      e.phoneNumber = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.';
    }
    if (!form.nationalID || !/^\d{12}$/.test(form.nationalID)) {
      e.nationalID = 'Số CCCD phải gồm đúng 12 chữ số.';
    }
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }

    setLoading(true);
    setApiError('');
    try {
      const res = await googleAuthService.googleRegister({ idToken, ...form });
      const { accessToken, role, fullName, userID, email } = res.data;
      onSuccess({ fullName, role, userID, email }, accessToken);
    } catch (err) {
      const status = err.response?.status;
      // Trích xuất câu thông báo lỗi gốc từ Spring Boot ném về
      const backendMessage = err.response?.data?.message || '';

      // Tùy thuộc vào cách GlobalExceptionHandler của bạn cấu hình, lỗi trùng lặp có thể là 409 (Conflict) hoặc 400 (Bad Request)
      if (status === 409 || status === 400) {

        // Phân tích câu chữ của Backend để bôi đỏ đúng ô Input bị lỗi
        if (backendMessage.includes('Username')) {
          setErrors({ userName: backendMessage });
        }
        else if (backendMessage.includes('Số điện thoại')) {
          setErrors({ phoneNumber: backendMessage });
        }
        else if (backendMessage.includes('CMND/CCCD')) {
          setErrors({ nationalID: backendMessage });
        }
        else if (backendMessage.includes('Email')) {
          // Email không có ô nhập liệu trên form này, nên ném lên Alert tổng
          setApiError(backendMessage);
        }
        else {
          // Fallback nếu câu lỗi bị thay đổi ở Backend mà Frontend chưa cập nhật kịp
          setApiError(backendMessage || 'Thông tin đã tồn tại. Vui lòng chọn thông tin khác.');
        }

      } else {
        setApiError('Có lỗi xảy ra từ máy chủ. Vui lòng thử lại sau.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    // Chỉ cho phép nhập số với phoneNumber và nationalID
    const numericFields = ['phoneNumber', 'nationalID'];
    const finalValue = numericFields.includes(name)
        ? value.replace(/\D/g, '')
        : value;

    setForm(p => ({ ...p, [name]: finalValue }));
    setErrors(p => ({ ...p, [name]: '' }));
    setApiError('');
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.stopPropagation()}>
      <div className="modal gcm-modal" onClick={(e) => e.stopPropagation()}>
        <div className="gcm-header">
          <img
            src="https://www.svgrepo.com/show/475656/google-color.svg"
            alt="Google"
            className="gcm-google-icon"
          />
          <div>
            <h3 className="gcm-title">Hoàn tất đăng ký</h3>
            <p className="gcm-subtitle">Vui lòng bổ sung thông tin để hoàn tất tạo tài khoản.</p>
          </div>
        </div>

        {apiError && (
          <div className="alert alert-error" style={{ marginBottom: 16 }}>
            <span>!</span> {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="gcm-form">
          <div className="form-group">
            <label className="form-label">Họ và tên</label>
            <div className="gcm-readonly-field">{googleProfile.fullName}</div>
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <div className="gcm-readonly-field">{googleProfile.email}</div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="gcm-userName">
              Tên đăng nhập <span className="required">*</span>
            </label>
            <input
              id="gcm-userName"
              name="userName"
              className={`form-input ${errors.userName ? 'error' : ''}`}
              placeholder="VD: patient_linh"
              value={form.userName}
              onChange={handleChange}
            />
            {errors.userName && <span className="form-error">! {errors.userName}</span>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="gcm-phone">
              Số điện thoại <span className="required">*</span>
            </label>
            <input
              id="gcm-phone"
              name="phoneNumber"
              type="text"
              maxLength={10}
              pattern="\d*"
              className={`form-input ${errors.phoneNumber ? 'error' : ''}`}
              placeholder="0901234567"
              value={form.phoneNumber}
              onChange={handleChange}
            />
            {errors.phoneNumber && <span className="form-error">! {errors.phoneNumber}</span>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="gcm-nid">
              Số CCCD <span className="required">*</span>
            </label>
            <input
              id="gcm-nid"
              name="nationalID"
              type="text"
              maxLength={12}
              pattern="\d*"
              className={`form-input ${errors.nationalID ? 'error' : ''}`}
              placeholder="012345678901"
              value={form.nationalID}
              onChange={handleChange}
            />
            {errors.nationalID && <span className="form-error">! {errors.nationalID}</span>}
          </div>

          <div className="gcm-actions">
            <button type="button" className="btn btn-outline" onClick={onCancel}>
              Hủy
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner" /> : null}
              {loading ? 'Đang tạo tài khoản...' : 'Hoàn tất đăng ký'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
