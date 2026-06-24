import { useState, useEffect, useContext } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import api from '../../services/api';
import { 
  Users, Search, Filter, Shield, ShieldOff, Play, Eye, 
  ChevronLeft, ChevronRight, PlusCircle, CheckCircle 
} from 'lucide-react';

export default function PatientList() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();
  
  // Tab detection from URL
  const queryParams = new URLSearchParams(location.search);
  const currentTabFromQuery = queryParams.get('tab') || 'active';
  const currentTab = location.pathname.endsWith('/create-session') ? 'create' : currentTabFromQuery;

  // State for Lists
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [sharedFilter, setSharedFilter] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Patient search & session creation states
  const [searchPatientKeyword, setSearchPatientKeyword] = useState('');
  const [foundPatients, setFoundPatients] = useState([]);
  const [searchingPatients, setSearchingPatients] = useState(false);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [createLoading, setCreateLoading] = useState(false);

  const fetchSessionsList = async () => {
    try {
      setLoading(true);
      const params = {
        page: page,
        size: 10
      };
      if (keyword.trim()) params.keyword = keyword.trim();
      if (sharedFilter !== '') params.isShared = sharedFilter === 'true';

      if (currentTab === 'active') {
        // Active sessions: ONLY created by current doctor
        const res = await api.get('/api/doctor/sessions', { params });
        const content = res.data.content || res.data.data?.content || [];
        const mapped = content
          .map(s => ({
            id: s.sessionId,
            patientName: s.fullName,
            visitDate: s.createdAt ? new Date(s.createdAt).toLocaleDateString('vi-VN') : 'N/A',
            isShared: s.isShared,
            status: s.status
          }))
          .filter(s => s.status !== 'COMPLETED');
        
        setSessions(mapped);
        setTotalPages(res.data.totalPages ?? res.data.data?.totalPages ?? 0);
        setTotalElements(res.data.totalElements ?? res.data.data?.totalElements ?? mapped.length);
      } else if (currentTab === 'completed') {
        // Completed sessions: ALL medical records in the hospital
        params.status = 'COMPLETED';
        const res = await api.get('/api/medical-records', { params });
        const content = res.data.content || res.data.data?.content || [];
        const mapped = content.map(s => ({
            id: s.sessionID || s.id,
          visitDate: s.visitDate,
          isShared: s.isShared,
          status: s.status
        }));
        
        setSessions(mapped);
        setTotalPages(res.data.totalPages ?? res.data.data?.totalPages ?? 0);
        setTotalElements(res.data.totalElements ?? res.data.data?.totalElements ?? mapped.length);
      }
    } catch (err) {
      console.error('Error fetching sessions filter', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (currentTab === 'active' || currentTab === 'completed') {
      fetchSessionsList();
    }
  }, [page, sharedFilter, currentTab]);

  // Reset page and keyword when tab changes
  useEffect(() => {
    setPage(0);
    setKeyword('');
    setSharedFilter('');
    setSessions([]);
    setSelectedPatient(null);
    setFoundPatients([]);
    setSearchPatientKeyword('');
  }, [currentTab]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchSessionsList();
  };

  // Search patients to initiate a session
  const handleSearchPatients = async (e) => {
    e.preventDefault();
    if (!searchPatientKeyword.trim()) return;
    try {
      setSearchingPatients(true);
      const res = await api.get('/api/diagnosis-sessions/search-patients', {
        params: { keyword: searchPatientKeyword }
      });
      setFoundPatients(res.data.data || []);
    } catch (e) {
      console.error(e);
      alert('Lỗi tìm kiếm bệnh nhân');
    } finally {
      setSearchingPatients(false);
    }
  };

  // Create session without height & weight
  const handleCreateSession = async (e) => {
    e.preventDefault();
    if (!selectedPatient) return;
    try {
      setCreateLoading(true);
      const res = await api.post('/api/diagnosis-sessions', {
        patientId: selectedPatient.patientId
        // height and weight omitted, letting them be null initially
      });
      
      const createdSession = res.data.data;
      setSelectedPatient(null);
      setFoundPatients([]);
      setSearchPatientKeyword('');
      
      // Navigate to the session detail page
      navigate(`/doctor/sessions/${createdSession.sessionId || createdSession.id}`);
    } catch (err) {
      console.error(err);
      alert('Tạo phiên khám không thành công. Bệnh nhân có thể đang có một phiên khám khác hoạt động.');
    } finally {
      setCreateLoading(false);
    }
  };

  // Toggle Sharing state directly from the list cell
  const handleToggleShare = async (sessionId, currentShared) => {
    try {
      const targetShared = !currentShared;
      await api.patch('/api/doctor/sessions/share', {
        sessionId: parseInt(sessionId),
        isShared: targetShared
      });
      setSessions(prevSessions => 
        prevSessions.map(s => s.id === sessionId ? { ...s, isShared: targetShared } : s)
      );
    } catch (err) {
      console.error('Error toggling share status', err);
      if (err.response?.status === 403) {
        alert('Bạn không có quyền thay đổi trạng thái công khai cho phiên khám này. Bác sĩ chỉ đổi được trạng thái chia sẻ khi đó là phiên khám do mình tạo ra.');
      } else {
        alert('Không thể cập nhật trạng thái công khai. Vui lòng thử lại.');
      }
    }
  };

  const getStatusBadge = (status) => {
    const cleanStatus = status?.toUpperCase() || '';
    if (cleanStatus === 'PENDING') return <span className="badge badge-pending">Đang chờ</span>;
    if (cleanStatus === 'PROCESSING') return <span className="badge badge-processing">Đang khám</span>;
    if (cleanStatus === 'COMPLETED') return <span className="badge badge-completed"><CheckCircle size={10} style={{ marginRight: '4px' }} />Hoàn thành</span>;
    return <span className="badge">{status}</span>;
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', width: '100%', textAlign: 'left' }}>
      
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '1.8rem', fontWeight: '700', color: '#100357' }}>
          {currentTab === 'create' && 'Tạo phiên khám mới'}
          {currentTab === 'active' && 'Quản lý phiên khám'}
          {currentTab === 'completed' && 'Hồ sơ bệnh án đã hoàn thành'}
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
          {currentTab === 'create' && 'Tìm kiếm bệnh nhân để khởi tạo hồ sơ bệnh án mới.'}
          {currentTab === 'active' && 'Xem danh sách và tiến hành chẩn đoán cho các phiên khám đang chờ.'}
          {currentTab === 'completed' && 'Xem lại danh sách bệnh án đã chẩn đoán hoàn thành.'}
        </p>
      </div>

      {/* CREATE TAB */}
      {currentTab === 'create' && (
        <div className="glass-panel" style={{ padding: '28px', border: '1px solid var(--border-color)', borderRadius: '16px' }}>
          {!selectedPatient ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <form onSubmit={handleSearchPatients} style={{ display: 'flex', gap: '12px' }}>
                <input 
                  type="text" 
                  className="form-control" 
                  placeholder="Tìm theo Tên hoặc Số CCCD/CMND..."
                  value={searchPatientKeyword}
                  onChange={(e) => setSearchPatientKeyword(e.target.value)}
                  required
                />
                <button type="submit" className="btn btn-primary" style={{ minWidth: '140px' }}>
                  <Search size={16} /> Tìm kiếm
                </button>
              </form>

              {searchingPatients ? (
                <div style={{ padding: '40px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                  Đang quét danh sách bệnh nhân...
                </div>
              ) : foundPatients.length > 0 ? (
                <div style={{ display: 'grid', gap: '12px', maxHeight: '350px', overflowY: 'auto' }}>
                  {foundPatients.map((p) => (
                    <div 
                      key={p.patientId}
                      onClick={() => setSelectedPatient(p)}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '16px 20px',
                        border: '1px solid var(--border-color)',
                        borderRadius: '12px',
                        cursor: 'pointer',
                        backgroundColor: '#ffffff',
                        transition: 'all 0.2s ease',
                        boxShadow: '0 2px 6px rgba(0,0,0,0.02)'
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.borderColor = '#100357'}
                      onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                    >
                      <div>
                        <strong style={{ display: 'block', fontSize: '1rem', color: '#100357' }}>{p.fullName}</strong>
                        <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                          Mã bệnh nhân: #{p.patientId} • CMND/CCCD: {p.nationalId}
                        </span>
                      </div>
                      <PlusCircle size={20} style={{ color: '#100357' }} />
                    </div>
                  ))}
                </div>
              ) : searchPatientKeyword && (
                <div style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                  Không tìm thấy bệnh nhân nào hợp lệ. Vui lòng kiểm tra lại thông tin.
                </div>
              )}
            </div>
          ) : (
            <form onSubmit={handleCreateSession} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ padding: '20px', backgroundColor: 'rgba(16, 3, 87, 0.05)', border: '1px solid rgba(16, 3, 87, 0.15)', borderRadius: '12px' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Bệnh nhân đã chọn</span>
                <strong style={{ display: 'block', fontSize: '1.2rem', color: '#100357' }}>{selectedPatient.fullName}</strong>
                <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)', display: 'block', marginTop: '4px' }}>
                  Mã bệnh nhân: #{selectedPatient.patientId} • CMND/CCCD: {selectedPatient.nationalId}
                </span>
                <span style={{ fontSize: '0.95rem', color: '#100357', fontWeight: '600', display: 'block', marginTop: '8px' }}>
                  Số ĐT: {selectedPatient.phoneNumber || 'Chưa cập nhật'}
                </span>
              </div>

              <div style={{ display: 'flex', gap: '16px' }}>
                <button type="button" onClick={() => setSelectedPatient(null)} className="btn btn-secondary" style={{ flex: 1, padding: '12px' }}>
                  Quay lại chọn bệnh nhân
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1, padding: '12px', backgroundColor: '#10b981', borderColor: '#10b981' }} disabled={createLoading}>
                  {createLoading ? 'Đang khởi tạo...' : 'Xác nhận khởi tạo'}
                </button>
              </div>
            </form>
          )}
        </div>
      )}

      {/* ACTIVE & COMPLETED LIST TAB */}
      {(currentTab === 'active' || currentTab === 'completed') && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* Filters */}
          <div className="glass-panel" style={{ padding: '20px', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
            <form onSubmit={handleSearchSubmit} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', alignItems: 'end' }}>
              
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Tìm kiếm bệnh nhân</label>
                <div style={{ position: 'relative' }}>
                  <input 
                    type="text" 
                    className="form-control" 
                    placeholder="Tên hoặc Số CMND/CCCD..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    style={{ paddingLeft: '38px' }}
                  />
                  <Search size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                </div>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Chế độ công khai kết quả</label>
                <select 
                  className="form-control"
                  value={sharedFilter}
                  onChange={(e) => setSharedFilter(e.target.value)}
                >
                  <option value="">Tất cả</option>
                  <option value="true">Công khai</option>
                  <option value="false">Riêng tư</option>
                </select>
              </div>

              <button type="submit" className="btn btn-primary" style={{ padding: '10px 20px' }}>
                <Filter size={16} /> Lọc kết quả
              </button>
            </form>
          </div>

          {/* List Table */}
          {loading ? (
            <div style={{ padding: '80px', textAlign: 'center', color: 'var(--text-muted)' }}>
              <div className="pulse-indicator" style={{ marginRight: '8px' }}></div>
              Đang tải danh sách phiên khám...
            </div>
          ) : sessions.length > 0 ? (
            <div className="custom-table-container glass-panel" style={{ borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
              <table className="custom-table">
                <thead>
                  <tr>
                    <th>Mã phiên</th>
                    <th>Bệnh nhân</th>
                    <th>Ngày khám</th>
                    <th>Trạng thái</th>
                    <th>Trạng thái chia sẻ</th>
                    <th style={{ textAlign: 'right' }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {sessions.map((session) => (
                    <tr key={session.id}>
                      <td style={{ fontWeight: '700' }}>#{session.id}</td>
                      <td style={{ fontWeight: '600' }}>{session.patientName}</td>
                      <td>{session.visitDate}</td>
                      <td>{getStatusBadge(session.status)}</td>
                      <td>
                        <button 
                          type="button"
                          onClick={() => handleToggleShare(session.id, session.isShared)}
                          disabled={session.status !== 'COMPLETED'}
                          style={{
                            background: 'none',
                            border: 'none',
                            cursor: session.status === 'COMPLETED' ? 'pointer' : 'not-allowed',
                            padding: '6px 12px',
                            borderRadius: '6px',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                            fontSize: '0.85rem',
                            fontWeight: '600',
                            color: session.isShared ? '#10b981' : '#f59e0b',
                            backgroundColor: session.isShared ? 'rgba(16, 185, 129, 0.1)' : 'rgba(245, 158, 11, 0.1)',
                            transition: 'all 0.2s ease',
                            outline: 'none',
                            opacity: session.status === 'COMPLETED' ? 1 : 0.6
                          }}
                          onMouseEnter={(e) => {
                            if (session.status === 'COMPLETED') {
                              e.currentTarget.style.backgroundColor = session.isShared ? 'rgba(16, 185, 129, 0.2)' : 'rgba(245, 158, 11, 0.2)';
                            }
                          }}
                          onMouseLeave={(e) => {
                            if (session.status === 'COMPLETED') {
                              e.currentTarget.style.backgroundColor = session.isShared ? 'rgba(16, 185, 129, 0.1)' : 'rgba(245, 158, 11, 0.1)';
                            }
                          }}
                          title={session.status === 'COMPLETED' ? 'Nhấp để thay đổi trạng thái' : 'Chỉ có thể công bố khi ca đã hoàn thành'}
                        >
                          {session.isShared ? (
                            <><Shield size={14} /> Công khai</>
                          ) : (
                            <><ShieldOff size={14} /> Riêng tư</>
                          )}
                        </button>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button 
                          onClick={() => navigate(`/doctor/sessions/${session.id}`)}
                          className="btn btn-secondary"
                          style={{ padding: '6px 14px', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                        >
                          {currentTab === 'active' ? (
                            <><Play size={12} fill="currentColor" /> Khám bệnh</>
                          ) : (
                            <><Eye size={12} /> Xem hồ sơ</>
                          )}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="glass-panel" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>
              <Users size={48} style={{ strokeWidth: '1.5px', marginBottom: '16px', color: 'hsl(var(--primary))' }} />
              <p>Không tìm thấy phiên khám nào phù hợp.</p>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Hiển thị {sessions.length} phiên khám
              </span>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button 
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  className="btn btn-secondary"
                  style={{ padding: '6px 12px' }}
                  disabled={page === 0}
                >
                  <ChevronLeft size={16} />
                </button>
                <span style={{ display: 'flex', alignItems: 'center', padding: '0 12px', fontSize: '0.9rem', fontWeight: '500' }}>
                  Trang {page + 1} / {totalPages}
                </span>
                <button 
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  className="btn btn-secondary"
                  style={{ padding: '6px 12px' }}
                  disabled={page === totalPages - 1}
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
