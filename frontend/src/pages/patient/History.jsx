import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import profileService from '../../services/profileService';
import medicalRecordService from '../../services/medicalRecordService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Calendar, FileText, CheckCircle2, ChevronRight, Shield, Award } from 'lucide-react';

export default function History() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRecords = async () => {
      setLoading(true);
      setError(null);
      try {
        const profileRes = await profileService.getProfile();
        setProfile(profileRes.data);

        const keyword = profileRes.data.nationalID || profileRes.data.fullName || undefined;
        if (keyword) {
          const recordsRes = await medicalRecordService.getRecords({ keyword, page: 0, size: 50 });
          const rawList = recordsRes.data.content || [];
          
          // Filter records to make sure they belong to this patient and status is COMPLETED
          const filtered = rawList.filter(record => 
            ((record.nationalID && record.nationalID === profileRes.data.nationalID) ||
            (record.patientName && record.patientName.trim().toLowerCase() === profileRes.data.fullName.trim().toLowerCase())) &&
            record.status === 'COMPLETED'
          );
          setRecords(filtered);
        }
      } catch (err) {
        setError(err?.response?.data?.message || 'Không thể tải lịch sử bệnh án.');
      } finally {
        setLoading(false);
      }
    };

    fetchRecords();
  }, []);

  const formatDate = (value) => value ? new Date(value).toLocaleDateString('vi-VN') : '—';

  if (loading) return <LoadingSpinner />;

  return (
    <main className="page z1">
      <div className="container" style={{ textAlign: 'left' }}>
        <div className="hero" style={{ marginBottom: '24px' }}>
          <h1 style={{ fontSize: '1.8rem', fontWeight: '700', color: 'var(--neutral-900)' }}>Hồ sơ bệnh án</h1>
          <p style={{ color: 'var(--neutral-500)', marginTop: '4px' }}>Danh sách hồ sơ bệnh án của bạn dựa trên thông tin cá nhân.</p>
        </div>

        <div className="mb-24">
          <Link to="/patient/home" className="btn btn-secondary btn-sm">
            ← Quay lại trang chính
          </Link>
        </div>

        {error && (
          <div className="empty" style={{ padding: '40px', textAlign: 'center', backgroundColor: '#fff', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
            <div className="empty-icon" style={{ fontSize: '2rem', marginBottom: '10px' }}>⚠️</div>
            <h3>Đã có lỗi xảy ra</h3>
            <p>{error}</p>
          </div>
        )}

        {records.length === 0 ? (
          <div className="empty" style={{ padding: '40px', textAlign: 'center', backgroundColor: '#fff', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
            <div className="empty-icon" style={{ fontSize: '2rem', marginBottom: '10px' }}>📭</div>
            <h3>Không tìm thấy hồ sơ bệnh án</h3>
            <p>Hãy chắc chắn rằng bạn đã có hồ sơ khám và thông tin cá nhân đã được cập nhật.</p>
          </div>
        ) : (
          <div className="records-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '24px' }}>
            {records.map(record => (
              <div 
                key={record.id} 
                className="bg-white rounded-2xl border border-gray-100 p-6 hover:border-blue-200 hover:shadow-lg hover:shadow-blue-50/40 transition-all duration-300 flex flex-col justify-between gap-5 relative cursor-pointer"
                style={{ textAlign: 'left', boxShadow: '0 4px 20px -2px rgba(50, 100, 150, 0.05)' }}
                onClick={() => navigate(`/patient/medical-record/${record.id}`)}
              >
                {/* Card Header */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '1px solid #f1f5f9', paddingBottom: '16px' }}>
                  <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                    <div style={{ padding: '8px', backgroundColor: '#eff6ff', borderRadius: '12px', color: '#3b82f6' }}>
                      <FileText className="w-5 h-5" />
                    </div>
                    <div>
                      <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontWeight: '700', textTransform: 'uppercase', tracking: 'wider' }}>Mã phiên khám</span>
                      <strong style={{ display: 'block', fontSize: '1.05rem', color: '#1e293b' }}>#{record.id}</strong>
                    </div>
                  </div>
                  <span style={{
                    backgroundColor: '#e8f5e9',
                    color: '#2e7d32',
                    padding: '6px 12px',
                    borderRadius: '9999px',
                    fontSize: '0.75rem',
                    fontWeight: '700',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}>
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Hoàn thành
                  </span>
                </div>

                {/* Card Body */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.9rem', color: '#475569' }}>
                    <Calendar className="w-4 h-4 text-blue-500" />
                    <span>Ngày khám: <strong style={{ color: '#1e293b' }}>{formatDate(record.visitDate)}</strong></span>
                  </div>

                  {record.symptoms && (
                    <div style={{ fontSize: '0.85rem', color: '#64748b', lineHeight: '1.5' }}>
                      <span style={{ fontWeight: '600', color: '#475569' }}>Triệu chứng ghi nhận:</span>
                      <p style={{ margin: '4px 0 0 0', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                        {record.symptoms}
                      </p>
                    </div>
                  )}

                  <div style={{ 
                    backgroundColor: '#f8fafc', 
                    borderRadius: '12px', 
                    padding: '12px 16px', 
                    border: '1px solid #f1f5f9' 
                  }}>
                    <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', display: 'block', marginBottom: '4px' }}>Chẩn đoán cuối cùng</span>
                    <strong style={{ color: record.isShared ? '#2563eb' : '#ef4444', fontSize: '0.95rem' }}>
                      {record.isShared 
                        ? (record.diagnosis || 'Chưa có chẩn đoán') 
                        : 'Bác sĩ chưa công bố'
                      }
                    </strong>
                  </div>
                </div>

                {/* Card Footer Action */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', marginTop: '4px', borderTop: '1px solid #f1f5f9', paddingTop: '12px' }}>
                  <span style={{ fontSize: '0.85rem', color: '#2563eb', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    Xem chi tiết bệnh án <ChevronRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
