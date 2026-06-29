import { useState, useEffect, useContext } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import api from '../../services/api';
import { 
  Users, Search, Filter, Shield, ShieldOff, Play, Eye, 
  ChevronLeft, ChevronRight, PlusCircle, CheckCircle, Plus, Phone, IdCard, ArrowLeft
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
    if (cleanStatus === 'PENDING') {
      return (
        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-100">
          <span className="w-1.5 h-1.5 bg-amber-500 rounded-full mr-1.5 animate-pulse" />
          Đang chờ
        </span>
      );
    }
    if (cleanStatus === 'PROCESSING') {
      return (
        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100">
          <span className="w-1.5 h-1.5 bg-blue-500 rounded-full mr-1.5 animate-pulse" />
          Đang khám
        </span>
      );
    }
    if (cleanStatus === 'COMPLETED') {
      return (
        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-100">
          <CheckCircle size={12} className="mr-1" />
          Hoàn thành
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-slate-50 text-slate-600 border border-slate-100">
        {status}
      </span>
    );
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', width: '100%', textAlign: 'left', padding: '32px' }}>
      
      <div style={{ marginBottom: '32px' }} className="border-b border-slate-100 pb-6">
        <h1 style={{ fontSize: '2rem', fontWeight: '850', color: '#100357', letterSpacing: '-0.03em' }} className="flex items-center gap-3">
          {currentTab === 'create' && <span className="bg-blue-50 text-blue-600 p-2 rounded-2xl"><PlusCircle size={28} /></span>}
          {currentTab === 'active' && <span className="bg-indigo-50 text-indigo-600 p-2 rounded-2xl"><Users size={28} /></span>}
          {currentTab === 'completed' && <span className="bg-emerald-50 text-emerald-600 p-2 rounded-2xl"><CheckCircle size={28} /></span>}
          <div>
            {currentTab === 'create' && 'Tạo phiên khám mới'}
            {currentTab === 'active' && 'Quản lý phiên khám'}
            {currentTab === 'completed' && 'Hồ sơ bệnh án đã hoàn thành'}
          </div>
        </h1>
        <p style={{ color: 'var(--neutral-500)', fontSize: '0.95rem', marginTop: '6px' }}>
          {currentTab === 'create' && 'Tìm kiếm bệnh nhân để khởi tạo hồ sơ bệnh án mới.'}
          {currentTab === 'active' && 'Xem danh sách và tiến hành chẩn đoán cho các phiên khám đang chờ.'}
          {currentTab === 'completed' && 'Xem lại danh sách bệnh án đã chẩn đoán hoàn thành.'}
        </p>
      </div>

      {/* CREATE TAB */}
      {currentTab === 'create' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {!selectedPatient ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              
              {/* Search bar container */}
              <div className="bg-slate-50/80 border border-slate-100 p-6 rounded-2xl">
                <form onSubmit={handleSearchPatients} style={{ display: 'flex', gap: '16px' }}>
                  <div className="relative flex-1">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                    <input 
                      type="text" 
                      className="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-[#100357] transition-all text-[0.95rem] shadow-sm font-medium"
                      placeholder="Tìm theo Tên hoặc Số CCCD/CMND..."
                      value={searchPatientKeyword}
                      onChange={(e) => setSearchPatientKeyword(e.target.value)}
                      required
                    />
                  </div>
                  <button 
                    type="submit" 
                    className="bg-[#100357] hover:bg-[#1b0880] text-white px-6 py-3 rounded-xl font-bold transition-all duration-200 flex items-center justify-center gap-2 shadow-md hover:shadow-lg active:scale-[0.98]"
                    style={{ minWidth: '150px' }}
                  >
                    <Search size={18} />
                    <span>Tìm kiếm</span>
                  </button>
                </form>
              </div>

              {searchingPatients ? (
                <div className="flex flex-col items-center justify-center py-16 text-slate-400 gap-3">
                  <div className="spinner spinner-dark" />
                  <span className="text-sm font-semibold animate-pulse text-slate-500">Đang quét danh sách bệnh nhân...</span>
                </div>
              ) : foundPatients.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider px-1">Kết quả tìm kiếm ({foundPatients.length})</div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '16px', maxHeight: '500px', overflowY: 'auto', padding: '4px' }}>
                    {foundPatients.map((p) => {
                      // Get name initials
                      const initials = p.fullName
                        ? p.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
                        : 'BN';
                      return (
                        <div 
                          key={p.patientId}
                          onClick={() => setSelectedPatient(p)}
                          className="group border border-slate-100 bg-white hover:border-[#100357]/20 rounded-xl p-5 cursor-pointer transition-all duration-300 hover:shadow-md hover:-translate-y-0.5 flex items-start justify-between gap-4"
                        >
                          <div className="flex items-center gap-4">
                            <div className="w-12 h-12 rounded-full bg-slate-100 group-hover:bg-[#100357]/5 text-slate-600 group-hover:text-[#100357] font-bold flex items-center justify-center text-sm transition-all duration-200">
                              {initials}
                            </div>
                            <div className="flex flex-col gap-1">
                              <span className="font-bold text-slate-800 group-hover:text-[#100357] text-[1.05rem] transition-colors duration-200">
                                {p.fullName}
                              </span>
                              <div className="flex items-center gap-2">
                                <span className="text-xs bg-slate-100 group-hover:bg-slate-200/50 text-slate-500 px-2 py-0.5 rounded font-medium">
                                  ID: #{p.patientId}
                                </span>
                                <span className="text-xs bg-slate-100 group-hover:bg-slate-200/50 text-slate-500 px-2 py-0.5 rounded font-medium">
                                  CCCD: {p.nationalId}
                                </span>
                              </div>
                            </div>
                          </div>
                          <div className="w-8 h-8 rounded-full bg-slate-50 group-hover:bg-[#100357] text-[#100357] group-hover:text-white flex items-center justify-center transition-all duration-200">
                            <Plus size={16} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              ) : searchPatientKeyword && (
                <div className="border border-dashed border-slate-200 rounded-2xl p-12 text-center text-slate-400 flex flex-col items-center justify-center gap-3">
                  <Users size={32} className="text-slate-300" />
                  <span className="text-sm font-semibold">Không tìm thấy bệnh nhân nào hợp lệ. Vui lòng kiểm tra lại thông tin.</span>
                </div>
              )}
            </div>
          ) : (
            <form onSubmit={handleCreateSession} className="max-w-2xl mx-auto w-full">
              <div className="bg-white border border-slate-150 rounded-2xl shadow-sm overflow-hidden">
                <div className="bg-gradient-to-r from-[#100357] to-[#1e0a8c] p-6 text-white">
                  <span className="text-xs font-bold tracking-widest uppercase opacity-75">Bệnh nhân được chọn chẩn đoán</span>
                  <h3 className="text-xl font-bold mt-1" style={{ color: '#ffffff' }}>{selectedPatient.fullName}</h3>
                </div>
                
                <div className="p-6 flex flex-col gap-5">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-slate-50 p-4 rounded-xl flex items-center gap-3">
                      <IdCard className="text-slate-400" size={20} />
                      <div className="flex flex-col">
                        <span className="text-xs text-slate-400 font-medium">CMND / CCCD</span>
                        <span className="text-[0.95rem] font-semibold text-slate-700">{selectedPatient.nationalId}</span>
                      </div>
                    </div>
                    
                    <div className="bg-slate-50 p-4 rounded-xl flex items-center gap-3">
                      <Phone className="text-slate-400" size={20} />
                      <div className="flex flex-col">
                        <span className="text-xs text-slate-400 font-medium">Số điện thoại</span>
                        <span className="text-[0.95rem] font-semibold text-slate-700">{selectedPatient.phoneNumber || 'Chưa cập nhật'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-blue-50/50 border border-blue-100 rounded-xl p-4 text-[0.875rem] text-blue-800 leading-relaxed">
                    <strong>Lưu ý:</strong> Phiên khám mới sẽ được khởi tạo với trạng thái <strong>Đang chờ</strong>. 
                    Bạn có thể cập nhật thông tin chiều cao, cân nặng và tiến hành chẩn đoán hình ảnh ở bước tiếp theo.
                  </div>
                </div>

                <div className="bg-slate-50 p-4 px-6 border-t border-slate-100 flex gap-4">
                  <button 
                    type="button" 
                    onClick={() => setSelectedPatient(null)} 
                    className="flex-1 border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 py-3 rounded-xl font-semibold transition-all duration-150 flex items-center justify-center gap-2"
                  >
                    <ArrowLeft size={16} />
                    <span>Quay lại</span>
                  </button>
                  <button 
                    type="submit" 
                    className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white py-3 rounded-xl font-semibold transition-all duration-150 flex items-center justify-center gap-2 shadow-sm"
                    disabled={createLoading}
                  >
                    {createLoading ? 'Đang khởi tạo...' : 'Xác nhận khởi tạo'}
                  </button>
                </div>
              </div>
            </form>
          )}
        </div>
      )}

      {/* ACTIVE & COMPLETED LIST TAB */}
      {(currentTab === 'active' || currentTab === 'completed') && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Filters */}
          <div className="bg-slate-50 border border-slate-100 p-5 rounded-2xl">
            <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              
              <div className="form-group">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Tìm kiếm bệnh nhân</label>
                <div className="relative">
                  <input 
                    type="text" 
                    className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#100357]/20 focus:border-[#100357] transition-all text-sm shadow-sm" 
                    placeholder="Tên hoặc Số CMND/CCCD..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                  />
                  <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                </div>
              </div>

              <div className="form-group">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Chế độ công khai kết quả</label>
                <select 
                  className="w-full px-3 py-2.5 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#100357]/20 focus:border-[#100357] transition-all text-sm shadow-sm cursor-pointer appearance-none"
                  value={sharedFilter}
                  onChange={(e) => setSharedFilter(e.target.value)}
                  style={{
                    backgroundImage: `url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none'%3E%3Cpath stroke='%236b7280' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
                    backgroundPosition: 'right 0.75rem center',
                    backgroundSize: '1.25rem',
                    backgroundRepeat: 'no-repeat',
                    paddingRight: '2.5rem'
                  }}
                >
                  <option value="">Tất cả</option>
                  <option value="true">Công khai</option>
                  <option value="false">Riêng tư</option>
                </select>
              </div>

              <button 
                type="submit" 
                className="bg-[#100357] hover:bg-[#1b0880] text-white py-2.5 rounded-xl font-bold transition-all duration-200 flex items-center justify-center gap-2 shadow-sm hover:shadow active:scale-[0.98] text-sm h-[44px]"
              >
                <Filter size={16} /> Lọc kết quả
              </button>
            </form>
          </div>

          {/* List Table */}
          {loading ? (
            <div className="flex flex-col items-center justify-center py-24 text-slate-400 gap-3">
              <div className="spinner spinner-dark" />
              <span className="text-sm font-semibold animate-pulse text-slate-500">Đang tải danh sách phiên khám...</span>
            </div>
          ) : sessions.length > 0 ? (
            <div className="border border-slate-100 rounded-2xl overflow-hidden shadow-sm bg-white">
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr className="bg-slate-50/50 border-b border-slate-100">
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider">Mã phiên</th>
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider">Bệnh nhân</th>
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider">Ngày khám</th>
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider">Trạng thái</th>
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider">Trạng thái chia sẻ</th>
                    <th className="p-4 px-6 text-xs font-bold text-slate-500 uppercase tracking-wider text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {sessions.map((session) => (
                    <tr key={session.id} className="hover:bg-slate-50/30 transition-colors duration-150">
                      <td className="p-4 px-6 text-sm font-bold text-[#100357]">#{session.id}</td>
                      <td className="p-4 px-6 text-sm font-semibold text-slate-800">{session.patientName}</td>
                      <td className="p-4 px-6 text-sm text-slate-600">{session.visitDate}</td>
                      <td className="p-4 px-6 text-sm">{getStatusBadge(session.status)}</td>
                      <td className="p-4 px-6 text-sm">
                        <button 
                          type="button"
                          onClick={() => handleToggleShare(session.id, session.isShared)}
                          disabled={session.status !== 'COMPLETED'}
                          className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition-all duration-200 ${
                            session.status === 'COMPLETED' 
                              ? 'cursor-pointer hover:scale-[1.02]' 
                              : 'cursor-not-allowed opacity-60'
                          } ${
                            session.isShared 
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' 
                              : 'bg-amber-50 text-amber-700 border border-amber-100'
                          }`}
                          title={session.status === 'COMPLETED' ? 'Nhấp để thay đổi trạng thái' : 'Chỉ có thể công bố khi ca đã hoàn thành'}
                        >
                          {session.isShared ? (
                            <><Shield size={13} /> Công khai</>
                          ) : (
                            <><ShieldOff size={13} /> Riêng tư</>
                          )}
                        </button>
                      </td>
                      <td className="p-4 px-6 text-sm text-right">
                        <button 
                          onClick={() => navigate(`/doctor/sessions/${session.id}`)}
                          className="border border-slate-200 hover:border-blue-200 hover:bg-blue-50/30 text-[#100357] hover:text-blue-700 px-4 py-2 rounded-xl text-xs font-bold inline-flex items-center gap-1.5 transition-all duration-150"
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
            <div className="border border-dashed border-slate-200 rounded-2xl p-16 text-center text-slate-400 flex flex-col items-center justify-center gap-4">
              <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-full flex items-center justify-center">
                <Users size={32} />
              </div>
              <div>
                <h4 className="text-slate-700 font-bold text-base mb-1">Không tìm thấy phiên khám</h4>
                <p className="text-sm text-slate-400">Không tìm thấy phiên khám nào phù hợp với bộ lọc hiện tại.</p>
              </div>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-between items-center mt-2 px-1">
              <span className="text-xs font-semibold text-slate-400">
                Hiển thị {sessions.length} phiên khám
              </span>
              <div className="flex gap-2 items-center">
                <button 
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  className="w-8 h-8 rounded-full border border-slate-200 hover:border-slate-300 bg-white text-slate-600 hover:bg-slate-50 flex items-center justify-center transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={page === 0}
                >
                  <ChevronLeft size={16} />
                </button>
                <span className="text-xs font-bold text-slate-600 px-2">
                  Trang {page + 1} / {totalPages}
                </span>
                <button 
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  className="w-8 h-8 rounded-full border border-slate-200 hover:border-slate-300 bg-white text-slate-600 hover:bg-slate-50 flex items-center justify-center transition-all disabled:opacity-50 disabled:cursor-not-allowed"
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
