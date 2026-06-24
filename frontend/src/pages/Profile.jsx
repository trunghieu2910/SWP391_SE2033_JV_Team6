import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import api from '../services/api';
import { User, Phone, Mail, Calendar, MapPin, Clipboard, Check, AlertCircle } from 'lucide-react';

export default function Profile() {
  const { updateUserInfo } = useContext(AuthContext);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Form states
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [nationalID, setNationalID] = useState('');
  const [gender, setGender] = useState('nam');
  const [dob, setDob] = useState('');
  const [address, setAddress] = useState('');

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const res = await api.get('/profile');
      const data = res.data;
      setProfile(data);
      
      // Prefill form
      setFullName(data.fullName || '');
      setPhoneNumber(data.phoneNumber || '');
      setNationalID(data.nationalID || '');
      setGender(data.gender || 'nam');
      setDob(data.dob || '');
      setAddress(data.address || '');
    } catch (err) {
      console.error('Error fetching profile', err);
      setError('Không thể tải thông tin hồ sơ của bạn.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    // Simple validation
    if (nationalID && nationalID.length !== 12) {
      setError('Số CMND/CCCD phải có độ dài đúng 12 ký tự.');
      return;
    }

    try {
      const updateData = {
        fullName,
        phoneNumber,
        nationalID,
        gender,
        dob: dob || null,
        address
      };
      
      const res = await api.put('/profile', updateData);
      setProfile(res.data);
      updateUserInfo({ fullName: res.data.fullName }); // Sync auth context
      setSuccess('Cập nhật hồ sơ thành công!');
      setEditing(false);
    } catch (err) {
      console.error(err);
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Cập nhật thất bại. Vui lòng kiểm tra lại thông tin.');
      }
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '80px', textAlign: 'center', color: 'var(--text-muted)' }}>
        <div className="pulse-indicator" style={{ marginRight: '8px' }}></div>
        Đang tải thông tin hồ sơ...
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '700px', margin: '30px auto', padding: '0 20px', width: '100%', textAlign: 'left' }}>
      
      <h1 style={{ fontSize: '1.8rem', fontFamily: 'var(--font-heading)', fontWeight: '700', marginBottom: '24px' }}>
        Thông tin tài khoản
      </h1>

      {error && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          backgroundColor: '#fee2e2',
          color: '#b91c1c',
          padding: '12px 16px',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.85rem',
          marginBottom: '20px',
          border: '1px solid rgba(239, 68, 68, 0.2)'
        }}>
          <AlertCircle size={18} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          backgroundColor: '#d1fae5',
          color: '#065f46',
          padding: '12px 16px',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.85rem',
          marginBottom: '20px',
          border: '1px solid rgba(5, 150, 105, 0.2)'
        }}>
          <Check size={18} style={{ flexShrink: 0 }} />
          <span>{success}</span>
        </div>
      )}

      <div className="glass-panel" style={{ padding: '30px', border: '1px solid var(--border-color)' }}>
        
        {!editing ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '16px', borderBottom: '1px solid var(--border-color)', paddingBottom: '20px' }}>
              <div className="info-avatar" style={{ marginBottom: 0 }}>
                {profile?.fullName ? profile.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <div>
                <h2 style={{ fontSize: '1.3rem', fontWeight: '700' }}>{profile?.fullName}</h2>
                <span style={{ fontSize: '0.85rem', color: 'hsl(var(--primary))', fontWeight: '600', textTransform: 'uppercase' }}>
                  Vai trò: {profile?.roleName === 'PATIENT' ? 'Bệnh nhân' : profile?.roleName === 'DOCTOR' ? 'Bác sĩ' : 'Quản trị viên'}
                </span>
              </div>
            </div>

            <div className="info-row">
              <span><User size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Tên đăng nhập (Username)</span>
              <strong>{profile?.username}</strong>
            </div>

            <div className="info-row">
              <span><Mail size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Email tài khoản</span>
              <strong>{profile?.email}</strong>
            </div>

            <div className="info-row">
              <span><Phone size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Số điện thoại</span>
              <strong>{profile?.phoneNumber || 'Chưa cập nhật'}</strong>
            </div>

            <div className="info-row">
              <span><Clipboard size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Số CCCD / CMND</span>
              <strong>{profile?.nationalID || 'Chưa cập nhật'}</strong>
            </div>

            <div className="info-row">
              <span><Calendar size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Ngày sinh</span>
              <strong>{profile?.dob ? new Date(profile.dob).toLocaleDateString('vi-VN') : 'Chưa cập nhật'}</strong>
            </div>

            <div className="info-row">
              <span><User size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Giới tính</span>
              <strong>{profile?.gender === 'nu' ? 'Nữ' : 'Nam'}</strong>
            </div>

            <div className="info-row" style={{ borderBottom: 'none' }}>
              <span><MapPin size={16} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Địa chỉ thường trú</span>
              <strong>{profile?.address || 'Chưa cập nhật'}</strong>
            </div>

            <button onClick={() => setEditing(true)} className="btn btn-primary" style={{ width: '100%', marginTop: '16px' }}>
              Chỉnh sửa thông tin
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h2 style={{ fontSize: '1.2rem', fontWeight: '700', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', marginBottom: '8px' }}>
              Cập nhật thông tin cá nhân
            </h2>

            <div className="form-group">
              <label className="form-label" htmlFor="fullName">Họ và tên</label>
              <input
                id="fullName"
                type="text"
                className="form-control"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
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
                <input
                  id="dob"
                  type="date"
                  className="form-control"
                  value={dob}
                  onChange={(e) => setDob(e.target.value)}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="phoneNumber">Số điện thoại</label>
                <input
                  id="phoneNumber"
                  type="tel"
                  className="form-control"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="nationalID">Số CCCD / CMND</label>
                <input
                  id="nationalID"
                  type="text"
                  className="form-control"
                  value={nationalID}
                  onChange={(e) => setNationalID(e.target.value)}
                  maxLength={12}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="address">Địa chỉ thường trú</label>
              <input
                id="address"
                type="text"
                className="form-control"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                required
              />
            </div>

            <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
              <button type="button" onClick={() => setEditing(false)} className="btn btn-secondary" style={{ flex: 1 }}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                Lưu thay đổi
              </button>
            </div>
          </form>
        )}

      </div>
    </div>
  );
}
