import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import medicalRecordService from '../../services/medicalRecordService';
import profileService from '../../services/profileService';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { AlertTriangle, Lock, Eye, Calendar, FileText, Activity } from 'lucide-react';

/* ---- Helpers ---- */
function calcAge(dob) {
  if (!dob) return '—';
  const diff = Date.now() - new Date(dob).getTime();
  return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25)) + ' tuổi';
}

function fmtDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function fmtDateTime(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleString('vi-VN');
}

function initials(name) {
  if (!name) return '?';
  const parts = name.trim().split(' ');
  return (parts[0][0] + (parts[parts.length - 1][0] || '')).toUpperCase();
}

export default function MedicalRecordDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [record, setRecord] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);

    const loadData = async () => {
      try {
        const [profileRes, recordRes] = await Promise.all([
          profileService.getProfile(),
          medicalRecordService.getRecordDetail(id)
        ]);

        if (active) {
          setProfile(profileRes.data);
          setRecord(recordRes.data);
        }
      } catch (err) {
        if (active) {
          setError(err?.response?.data?.message || 'Không tìm thấy bệnh án hoặc lỗi tải thông tin.');
        }
      } finally {
        if (active) setLoading(false);
      }
    };

    loadData();
      
    return () => {
      active = false;
    };
  }, [id]);

  if (loading) return <LoadingSpinner />;

  if (error || !record) {
    return (
      <main className="page z1">
        <div className="container" style={{ textAlign: 'left' }}>
          <div className="empty" style={{ padding: '40px', textAlign: 'center', backgroundColor: '#fff', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
            <div className="empty-icon" style={{ fontSize: '2rem', marginBottom: '10px' }}>❌</div>
            <h3>Không tìm thấy bệnh án</h3>
            <p>{error || `Phiên khám #${id} không tồn tại trong hệ thống.`}</p>
            <button className="btn btn-secondary mt-16" onClick={() => navigate('/patient/medical-records')}>
              ← Quay lại
            </button>
          </div>
        </div>
      </main>
    );
  }

  // Verification Check: Bệnh nhân chỉ xem được hồ sơ bệnh án của chính mình
  const isOwner = () => {
    if (!profile || !record) return false;
    
    // So khớp theo CCCD/CMND nếu cả 2 có
    if (profile.nationalID && record.patientNationalID) {
      return profile.nationalID.trim() === record.patientNationalID.trim();
    }
    
    // So khớp theo Số điện thoại nếu cả 2 có
    if (profile.phoneNumber && record.patientPhone) {
      return profile.phoneNumber.trim() === record.patientPhone.trim();
    }

    // So khớp theo Họ và tên nếu cả 2 có
    if (profile.fullName && record.patientFullName) {
      return profile.fullName.trim().toLowerCase() === record.patientFullName.trim().toLowerCase();
    }

    return false;
  };

  if (!isOwner() || record.status !== 'COMPLETED') {
    return (
      <main className="page z1">
        <div className="container" style={{ textAlign: 'left' }}>
          <div className="empty" style={{ padding: '40px', textAlign: 'center', backgroundColor: '#fff', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
            <div className="empty-icon" style={{ fontSize: '2.5rem', marginBottom: '10px' }}>🚫</div>
            <h3>Truy cập bị từ chối</h3>
            <p>Bạn không có quyền xem hồ sơ bệnh án này hoặc phiên khám chưa hoàn thành.</p>
            <button className="btn btn-secondary mt-16" onClick={() => navigate('/patient/medical-records')}>
              ← Quay lại danh sách
            </button>
          </div>
        </div>
      </main>
    );
  }

  const {
    sessionID, createdAt, status, weight, height, isShared,
    patientFullName, patientNationalID, patientGender, patientDob, patientAddress, patientPhone,
    doctorFullName,
    symptomResultID, symptomResultStatus, symptoms,
    menopauseStatus, symptomDuration, symptomProgressing,
    reviewID, finalDiagnosis, treatmentPlan, doctorAdvice, note, reviewedAt, reviewedByDoctorName,
  } = record;

  const name = patientFullName ?? 'Không rõ';

  return (
    <main className="page z1">
      <div className="container" style={{ textAlign: 'left' }}>
        
        {/* Back Button */}
        <div className="mb-24">
          <button 
            className="btn btn-secondary btn-sm" 
            onClick={() => navigate('/patient/medical-records')}
          >
            ← Quay lại danh sách
          </button>
        </div>

        {/* ── PATIENT BANNER ── */}
        <div className="patient-banner glass-panel" style={{ padding: '24px', marginBottom: '24px', display: 'flex', flexDirection: 'column', mdDirection: 'row', gap: '20px', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
            <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
              <div className="info-avatar" style={{ marginBottom: 0 }}>
                {initials(name)}
              </div>
              <div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: '700', margin: 0 }}>{name}</h2>
                <div style={{ display: 'flex', gap: '12px', fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                  <span>CCCD: {patientNationalID || '—'}</span>
                  <span>•</span>
                  <span>Giới tính: {patientGender === 'Female' ? 'Nữ' : 'Nam'}</span>
                  <span>•</span>
                  <span>Tuổi: {calcAge(patientDob)}</span>
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
              <span className={`badge ${isShared ? 'badge-completed' : 'badge-processing'}`} style={{
                backgroundColor: isShared ? '#e8f5e9' : '#fee2e2',
                color: isShared ? '#2e7d32' : '#dc2626',
                padding: '4px 10px',
                borderRadius: '20px',
                fontSize: '0.75rem',
                fontWeight: '700'
              }}>
                {isShared ? '🔗 Đã công bố' : '🔒 Chưa công bố'}
              </span>
            </div>
          </div>
        </div>

        {/* ── 1. CLINICAL GENERAL INFO ── */}
        <div className="glass-panel mb-24" style={{ padding: '24px', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Activity className="w-5 h-5 text-primary" /> Thông tin khám bệnh
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div className="info-row" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Ngày khám</span>
              <strong>{fmtDateTime(createdAt)}</strong>
            </div>
            <div className="info-row" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Bác sĩ phụ trách</span>
              <strong>{doctorFullName || '—'}</strong>
            </div>
            <div className="info-row" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Chiều cao / Cân nặng</span>
              <strong>{height ? `${height} cm` : '—'} / {weight ? `${weight} kg` : '—'}</strong>
            </div>
            <div className="info-row" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Tình trạng triệu chứng</span>
              <strong>{symptoms && symptoms.length > 0 ? symptoms.map(s => s.symptomName).join(', ') : 'Không ghi nhận triệu chứng'}</strong>
            </div>
          </div>
        </div>

        {/* ── 2. DIAGNOSIS RESULTS (WITH PUBLISH CHECK) ── */}
        <div className="glass-panel" style={{ padding: '24px', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <FileText className="w-5 h-5 text-primary" /> Kết quả chẩn đoán & điều trị
          </h3>

          {!isShared ? (
            /* UNPUBLISHED STATE */
            <div style={{ padding: '30px', textAlign: 'center', backgroundColor: '#fafafa', borderRadius: 'var(--radius-sm)', border: '1px dashed #ddd', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
              <Lock className="w-10 h-10 text-red-500 mb-2" />
              <h4 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--neutral-900)', margin: 0 }}>Chưa có kết quả chẩn đoán</h4>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', margin: 0 }}>Bác sĩ chưa công bố hồ sơ bệnh án</p>
            </div>
          ) : (
            /* PUBLISHED STATE */
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
                <div style={{ backgroundColor: '#fcfcfc', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '16px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>🔬 Chẩn đoán của bác sĩ</span>
                  <div style={{ fontSize: '1.15rem', fontWeight: '700', color: 'hsl(var(--primary))', marginTop: '6px' }}>
                    {finalDiagnosis || 'Chưa ghi nhận chẩn đoán cuối cùng'}
                  </div>
                </div>
                
                <div style={{ backgroundColor: '#fcfcfc', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '16px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>💊 Đơn thuốc & Phác đồ</span>
                  <div style={{ fontSize: '0.95rem', fontWeight: '600', color: 'var(--neutral-700)', marginTop: '6px', whiteSpace: 'pre-wrap' }}>
                    {treatmentPlan || 'Chưa ghi nhận đơn thuốc'}
                  </div>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
                <div style={{ backgroundColor: '#fcfcfc', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '16px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>💡 Hướng dẫn & Lời khuyên</span>
                  <div style={{ fontSize: '0.95rem', fontWeight: '600', color: 'var(--neutral-700)', marginTop: '6px', whiteSpace: 'pre-wrap' }}>
                    {doctorAdvice || 'Chưa ghi nhận lời khuyên'}
                  </div>
                </div>

                <div style={{ backgroundColor: '#fcfcfc', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '16px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>📎 Ghi chú thêm</span>
                  <div style={{ fontSize: '0.95rem', fontWeight: '600', color: 'var(--neutral-700)', marginTop: '6px', whiteSpace: 'pre-wrap' }}>
                    {note || 'Không có ghi chú'}
                  </div>
                </div>
              </div>

              {reviewedByDoctorName && (
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', borderTop: '1px solid var(--border-color)', paddingTop: '12px', textAlign: 'right' }}>
                  Được kết luận bởi <strong>{reviewedByDoctorName}</strong> lúc <strong>{fmtDateTime(reviewedAt)}</strong>
                </div>
              )}
            </div>
          )}
        </div>

      </div>
    </main>
  );
}
