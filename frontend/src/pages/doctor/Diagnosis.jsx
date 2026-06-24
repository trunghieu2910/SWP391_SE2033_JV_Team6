import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import api from '../../services/api';
import ClinicalSymptomsForm from '../../components/doctor/session-detail/ClinicalSymptomsForm';
import { 
  ArrowLeft, Activity, User, Clipboard, Calendar, Clock, 
  FlaskConical, CheckCircle, Shield, ShieldOff, AlertTriangle, PlusCircle, Check,
  ChevronDown, ChevronUp, Image as ImageIcon, Trash2, Cpu, Eye, RefreshCw, Upload, FileText
} from 'lucide-react';

// Parameter templates for each lab test type
const TEST_PARAM_TEMPLATES = {
  'Xét nghiệm máu tổng quát': [
    { name: 'RBC (Hồng cầu)', key: 'rbc', defaultVal: '4.5', unit: 'T/L', refRange: '3.8 - 5.8' },
    { name: 'WBC (Bạch cầu)', key: 'wbc', defaultVal: '6.2', unit: 'G/L', refRange: '4.0 - 10.0' },
    { name: 'Hb (Hemoglobin)', key: 'hb', defaultVal: '135', unit: 'g/L', refRange: '120 - 160' },
    { name: 'PLT (Tiểu cầu)', key: 'plt', defaultVal: '250', unit: 'G/L', refRange: '150 - 400' }
  ],
  'Xét nghiệm Pap Smear': [
    { name: 'Tế bào biểu mô vảy', key: 'squamous', defaultVal: 'Bình thường', unit: '', refRange: 'Bình thường' },
    { name: 'Tế bào biểu mô tuyến', key: 'glandular', defaultVal: 'Bình thường', unit: '', refRange: 'Bình thường' },
    { name: 'Phân loại Bethesda', key: 'bethesda', defaultVal: 'NILM (Âm tính với tổn thương)', unit: '', refRange: 'NILM' }
  ],
  'Xét nghiệm HPV DNA': [
    { name: 'HPV Type 16', key: 'hpv16', defaultVal: 'Âm tính', unit: '', refRange: 'Âm tính' },
    { name: 'HPV Type 18', key: 'hpv18', defaultVal: 'Âm tính', unit: '', refRange: 'Âm tính' },
    { name: 'HPV High-risk group khác', key: 'hpvOther', defaultVal: 'Âm tính', unit: '', refRange: 'Âm tính' }
  ],
  'Soi cổ tử cung (Colposcopy)': [
    { name: 'Hình ảnh ranh giới SCJ', key: 'scjImage', defaultVal: 'Rõ ràng (Type 1)', unit: '', refRange: 'Rõ ràng' },
    { name: 'Biểu mô trắng (Acetowhite)', key: 'acetowhite', defaultVal: 'Không phát hiện', unit: '', refRange: 'Không phát hiện' },
    { name: 'Mạch máu bất thường', key: 'vessels', defaultVal: 'Không có', unit: '', refRange: 'Không có' }
  ],
  'Sinh thiết cổ tử cung (Biopsy)': [
    { name: 'Kết quả giải phẫu bệnh', key: 'pathology', defaultVal: 'Viêm cổ tử cung mãn tính', unit: '', refRange: 'Bình thường' },
    { name: 'Mức độ loạn sản', key: 'dysplasia', defaultVal: 'Không phát hiện (CIN 0)', unit: '', refRange: 'Không phát hiện' }
  ]
};

export default function Diagnosis() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  // Core API states
  const [session, setSession] = useState(null);
  const [symptomResult, setSymptomResult] = useState(null);
  const [labResults, setLabResults] = useState([]);
  
  // Loading states
  const [loading, setLoading] = useState(true);
  const [formSubmitLoading, setFormSubmitLoading] = useState(false);
  const [labSubmitLoading, setLabSubmitLoading] = useState(false);
  const [statusSubmitLoading, setStatusSubmitLoading] = useState(false);
  const [shareSubmitLoading, setShareSubmitLoading] = useState(false);
  
  // Input states
  const [newLabType, setNewLabType] = useState('Xét nghiệm máu tổng quát');
  const [message, setMessage] = useState(null);

  // Redesign Collapsible box states
  const [showSymptoms, setShowSymptoms] = useState(false);
  const [showUltrasound, setShowUltrasound] = useState(false);
  const [showLab, setShowLab] = useState(false);
  const [isEditingSymptoms, setIsEditingSymptoms] = useState(false);

  // Redesign Interactive Simulated state (persisted via localStorage per sessionId)
  const [ultrasoundScans, setUltrasoundScans] = useState([]);
  const [scanFile, setScanFile] = useState(null);
  const [scanPreviewUrl, setScanPreviewUrl] = useState('');
  const [scanDesc, setScanDesc] = useState('');
  const [uploadingScan, setUploadingScan] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  // AI simulated analysis states
  const [analyzingScanId, setAnalyzingScanId] = useState(null);
  const [analysisProgress, setAnalysisProgress] = useState(0);
  const [analysisLogs, setAnalysisLogs] = useState([]);

  // Lab parameters local storage mock
  const [labParams, setLabParams] = useState({});
  const [editingLabId, setEditingLabId] = useState(null);
  const [labInputParams, setLabInputParams] = useState({});

  // Verdict local storage mock
  const [diagnosisConclusion, setDiagnosisConclusion] = useState('');
  const [treatmentPlan, setTreatmentPlan] = useState('');

  // 1. Fetch data from backend
  const fetchSessionDetails = async () => {
    try {
      setLoading(true);
      
      // Get session info
      const sessionRes = await api.get(`/api/diagnosis-sessions/${sessionId}`);
      setSession(sessionRes.data.data);

      // Get symptom results (if exists)
      try {
        const symptomRes = await api.get(`/api/diagnosis-sessions/${sessionId}/symptom-result`);
        setSymptomResult(symptomRes.data.data);
      } catch (e) {
        console.warn('No symptoms recorded yet for this session');
      }

      // Get lab tests
      const labRes = await api.get(`/api/lab-results/session/${sessionId}`);
      setLabResults(labRes.data.data || []);

    } catch (err) {
      console.error('Error loading session details', err);
      setMessage({ type: 'error', text: 'Không thể tải chi tiết phiên khám này.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSessionDetails();
  }, [sessionId]);

  // 2. Load Local Storage state (ultrasound, lab parameters, diagnostic conclusion)
  useEffect(() => {
    if (sessionId) {
      // Load scans
      const savedScans = localStorage.getItem(`ultrasound_scans_${sessionId}`);
      if (savedScans) {
        setUltrasoundScans(JSON.parse(savedScans));
      } else {
        // Default mock scans
        const defaults = [
          {
            id: 'scan_default_1',
            url: 'https://images.unsplash.com/photo-1579684389782-64d84b5e901a?auto=format&fit=crop&q=80&w=400',
            description: 'Ảnh chụp âm đạo cổ tử cung - Ranh giới SCJ rõ nét loại 1, bề mặt láng mịn.',
            createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
            aiClassified: 'Bình thường (Normal)',
            confidence: '96.2%'
          }
        ];
        setUltrasoundScans(defaults);
        localStorage.setItem(`ultrasound_scans_${sessionId}`, JSON.stringify(defaults));
      }

      // Load lab parameters
      const savedLabParams = localStorage.getItem(`lab_parameters_${sessionId}`);
      if (savedLabParams) {
        setLabParams(JSON.parse(savedLabParams));
      }

      // Load verdict
      const savedConclusion = localStorage.getItem(`verdict_conclusion_${sessionId}`);
      if (savedConclusion) setDiagnosisConclusion(savedConclusion);

      const savedPlan = localStorage.getItem(`verdict_plan_${sessionId}`);
      if (savedPlan) setTreatmentPlan(savedPlan);
    }
  }, [sessionId]);

  // Persist scans changes
  const saveScansToLocal = (newScans) => {
    setUltrasoundScans(newScans);
    localStorage.setItem(`ultrasound_scans_${sessionId}`, JSON.stringify(newScans));
  };

  // Submit Symptoms questionnaire
  const handleSymptomsSubmit = async (formData) => {
    setFormSubmitLoading(true);
    setMessage(null);
    try {
      await api.put(`/api/diagnosis-sessions/${sessionId}/symptom-result`, formData);
      setMessage({ type: 'success', text: 'Cập nhật thông tin triệu chứng bệnh nhân thành công!' });
      setIsEditingSymptoms(false);
      await fetchSessionDetails();
    } catch (err) {
      console.error(err);
      setMessage({ type: 'error', text: 'Gặp lỗi khi lưu biểu mẫu triệu chứng.' });
    } finally {
      setFormSubmitLoading(false);
    }
  };

  // Add Lab Test
  const handleAddLabResult = async (e) => {
    e.preventDefault();
    if (!newLabType) return;
    setLabSubmitLoading(true);
    setMessage(null);
    try {
      await api.post('/api/lab-results', {
        sessionId: parseInt(sessionId),
        testType: newLabType,
        parameters: []
      });
      setMessage({ type: 'success', text: `Tạo chỉ định xét nghiệm "${newLabType}" thành công!` });
      
      // Refresh lab list
      const labRes = await api.get(`/api/lab-results/session/${sessionId}`);
      setLabResults(labRes.data.data || []);
    } catch (err) {
      console.error(err);
      setMessage({ type: 'error', text: 'Không thể chỉ định xét nghiệm. Vui lòng thử lại.' });
    } finally {
      setLabSubmitLoading(false);
    }
  };

  // Toggle Share status (Công khai vs Riêng tư)
  const handleToggleShare = async (targetShared) => {
    setShareSubmitLoading(true);
    setMessage(null);
    try {
      await api.patch('/api/doctor/sessions/share', {
        sessionId: parseInt(sessionId),
        isShared: targetShared
      });
      setSession(prev => ({ ...prev, isShared: targetShared }));
      setMessage({ type: 'success', text: targetShared ? 'Đã chuyển phiên khám sang trạng thái CÔNG KHAI!' : 'Đã chuyển phiên khám sang trạng thái RIÊNG TƯ!' });
    } catch (err) {
      console.error(err);
      if (err.response?.status === 403) {
        setMessage({ type: 'error', text: 'Bạn không có quyền thay đổi trạng thái công khai cho phiên khám này. Bác sĩ chỉ đổi được trạng thái chia sẻ khi đó là phiên khám do mình tạo ra.' });
      } else {
        setMessage({ type: 'error', text: 'Không thể cập nhật trạng thái chia sẻ. Vui lòng thử lại.' });
      }
    } finally {
      setShareSubmitLoading(false);
    }
  };

  // Complete Session Status
  const handleCompleteSession = async () => {
    setStatusSubmitLoading(true);
    setMessage(null);
    try {
      // 1. Send complete status to backend
      await api.patch('/api/doctor/sessions/status', {
        sessionId: parseInt(sessionId),
        status: 'COMPLETED'
      });
      setSession(prev => ({ ...prev, status: 'COMPLETED' }));

      // 2. Persist diagnosis details in local storage
      localStorage.setItem(`verdict_conclusion_${sessionId}`, diagnosisConclusion);
      localStorage.setItem(`verdict_plan_${sessionId}`, treatmentPlan);

      setMessage({ type: 'success', text: 'Đã hoàn thành phiên khám và lưu trữ bệnh án thành công!' });
    } catch (err) {
      console.error(err);
      setMessage({ type: 'error', text: 'Không thể cập nhật trạng thái phiên khám. Vui lòng thử lại.' });
    } finally {
      setStatusSubmitLoading(false);
    }
  };

  // Upload scan image simulation
  const handleScanFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setScanFile(file);
      setScanPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleUploadScanSubmit = (e) => {
    e.preventDefault();
    if (!scanPreviewUrl) return;

    setUploadingScan(true);
    setUploadProgress(0);

    const interval = setInterval(() => {
      setUploadProgress(p => {
        if (p >= 100) {
          clearInterval(interval);
          return 100;
        }
        return p + 10;
      });
    }, 150);

    setTimeout(() => {
      const newScan = {
        id: 'scan_' + Date.now(),
        url: scanPreviewUrl,
        description: scanDesc || 'Không có mô tả chi tiết.',
        createdAt: new Date().toISOString(),
        aiClassified: null,
        confidence: null
      };

      const updated = [...ultrasoundScans, newScan];
      saveScansToLocal(updated);
      
      setUploadingScan(false);
      setScanFile(null);
      setScanPreviewUrl('');
      setScanDesc('');
    }, 1700);
  };

  const handleDeleteScan = (scanId) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa ảnh này?')) {
      const updated = ultrasoundScans.filter(s => s.id !== scanId);
      saveScansToLocal(updated);
    }
  };

  // Run AI analysis on scan simulation
  const handleRunAiAnalysis = (scanId) => {
    setAnalyzingScanId(scanId);
    setAnalysisProgress(0);
    setAnalysisLogs([]);

    const steps = [
      { delay: 600, text: '🔍 [VIA Screening] Khởi tạo dữ liệu hình ảnh, khử nhiễu...' },
      { delay: 1300, text: '🎯 [Segmentation] Xác định vùng tiếp giáp biểu mô SCJ (Transformation Zone)...' },
      { delay: 2000, text: '🔬 [Feature Analysis] Nhận diện vùng trắng acetowhite và cấu trúc mạch máu...' },
      { delay: 2700, text: '🧠 [Deep Learning] Suy luận phân loại qua mô hình mạng CNN ResNet101...' }
    ];

    steps.forEach((step, idx) => {
      setTimeout(() => {
        setAnalysisLogs(prev => [...prev, step.text]);
        setAnalysisProgress(Math.round(((idx + 1) / steps.length) * 100));

        if (idx === steps.length - 1) {
          setTimeout(() => {
            const isAbnormal = Math.random() > 0.45;
            const updated = ultrasoundScans.map(s => {
              if (s.id === scanId) {
                return {
                  ...s,
                  aiClassified: isAbnormal ? 'Nghi ngờ tổn thương (Abnormal)' : 'Bình thường (Normal)',
                  confidence: (86 + Math.random() * 12).toFixed(1) + '%',
                  details: isAbnormal 
                    ? 'Phát hiện mảng acetowhite dày không đều với ranh giới rõ rệt ở vị trí 9 giờ. Nghi ngờ CIN II/III.' 
                    : 'Bề mặt trơn mịn, biểu mô đồng nhất, phản ứng axetic âm tính hoặc biến mất nhanh. Ranh giới SCJ rõ nét.'
                };
              }
              return s;
            });
            saveScansToLocal(updated);
            setAnalyzingScanId(null);
          }, 600);
        }
      }, step.delay);
    });
  };

  // Lab Parameter Entry Action
  const handleEditParamsClick = (lab) => {
    setEditingLabId(lab.labResultId);
    const template = TEST_PARAM_TEMPLATES[lab.testType] || [
      { name: 'Kết quả chung', key: 'generalResult', defaultVal: 'Bình thường', unit: '', refRange: 'Bình thường' }
    ];
    const initial = {};
    template.forEach(p => {
      initial[p.key] = labParams[lab.labResultId]?.[p.key] ?? p.defaultVal;
    });
    setLabInputParams(initial);
  };

  const handleSaveParams = (labId) => {
    const nextParams = {
      ...labParams,
      [labId]: {
        ...labInputParams,
        updatedAt: new Date().toISOString()
      }
    };
    setLabParams(nextParams);
    localStorage.setItem(`lab_parameters_${sessionId}`, JSON.stringify(nextParams));
    setEditingLabId(null);
  };

  if (loading) {
    return (
      <div style={{ padding: '80px', textAlign: 'center', color: 'var(--text-muted)' }}>
        <div className="pulse-indicator" style={{ marginRight: '8px' }}></div>
        Đang tải dữ liệu hồ sơ khám bệnh nhân...
      </div>
    );
  }

  const isCompleted = session?.status === 'COMPLETED';
  const hasSymptoms = symptomResult && symptomResult.symptomIds && symptomResult.symptomIds.length > 0;
  const activeTab = session?.status === 'COMPLETED' ? 'completed' : 'active';

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '16px 0', width: '100%', textAlign: 'left' }}>
      
      {/* Navigation */}
      <Link 
        to={`/doctor/patients?tab=${activeTab}`} 
        style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px', fontWeight: '500' }}
      >
        <ArrowLeft size={16} /> Quay lại danh sách khám
      </Link>

      {/* Workspace Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-color)', paddingBottom: '16px', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <span style={{ fontSize: '0.8rem', color: '#100357', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: '700' }}>
            Không gian làm việc của bác sĩ
          </span>
          <h1 style={{ fontSize: '1.8rem', fontFamily: 'var(--font-heading)', fontWeight: '800', marginTop: '2px', color: '#100357' }}>
            Bệnh án lâm sàng: {session?.patientName}
          </h1>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Mã phiên #{session?.sessionId} • {isCompleted ? 'Đã hoàn thành bệnh án' : 'Đang xử lý chẩn đoán'}</span>
        </div>

        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          {session?.status === 'PENDING' && <span className="badge badge-pending" style={{ padding: '6px 12px' }}>Chờ khám</span>}
          {session?.status === 'PROCESSING' && <span className="badge badge-processing" style={{ padding: '6px 12px' }}>Đang khám</span>}
          {session?.status === 'COMPLETED' && <span className="badge badge-completed" style={{ padding: '6px 12px' }}>Đã hoàn thành</span>}
        </div>
      </div>

      {message && (
        <div className="glass-panel" style={{
          padding: '12px 20px',
          backgroundColor: message.type === 'success' ? '#d1fae5' : '#fee2e2',
          color: message.type === 'success' ? '#065f46' : '#b91c1c',
          border: `1px solid ${message.type === 'success' ? 'rgba(5, 150, 105, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`,
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.85rem',
          marginBottom: '24px'
        }}>
          {message.text}
        </div>
      )}

      {/* Main Two-Column Layout */}
      <div style={{ display: 'flex', gap: '28px', flexWrap: 'wrap', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: Vertical Profile Card */}
        <div style={{ 
          flex: '0 0 320px', 
          minWidth: '320px', 
          display: 'flex', 
          flexDirection: 'column', 
          gap: '24px',
          position: 'sticky',
          top: '20px',
          maxHeight: 'calc(100vh - 40px)',
          overflowY: 'auto'
        }}>
          <div className="glass-panel" style={{ padding: '24px', border: '1px solid var(--border-color)', borderRadius: '16px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* Avatar block */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', borderBottom: '1px solid var(--border-color)', paddingBottom: '20px' }}>
              <div style={{
                width: '72px',
                height: '72px',
                borderRadius: '50%',
                backgroundColor: 'rgba(16, 3, 87, 0.05)',
                color: '#100357',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.8rem',
                fontWeight: '800',
                marginBottom: '12px',
                border: '2px solid rgba(16, 3, 87, 0.1)'
              }}>
                {session?.patientName ? session.patientName.charAt(0).toUpperCase() : 'P'}
              </div>
              <h2 style={{ fontSize: '1.2rem', fontWeight: '700', margin: '0 0 4px 0', color: '#100357' }}>{session?.patientName}</h2>
              <span className="badge badge-processing" style={{ fontSize: '0.75rem', fontWeight: '600', backgroundColor: 'rgba(16, 3, 87, 0.1)', color: '#100357' }}>
                Mã BN: #{session?.patientId}
              </span>
            </div>

            {/* Vertical details list */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', textAlign: 'left' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: '500' }}>Ngày sinh</span>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>
                  {session?.dob ? new Date(session.dob).toLocaleDateString('vi-VN') : 'Chưa điền'}
                </strong>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: '500' }}>Số điện thoại</span>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>
                  {session?.phoneNumber || 'Chưa điền'}
                </strong>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: '500' }}>Số CMND/CCCD</span>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>
                  {session?.nationalID || 'Chưa điền'}
                </strong>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: '500' }}>Chiều cao</span>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>
                  {session?.height ? `${session.height} cm` : 'Chưa điền'}
                </strong>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: '500' }}>Cân nặng</span>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>
                  {session?.weight ? `${session.weight} kg` : 'Chưa điền'}
                </strong>
              </div>
            </div>

            {/* Sharing toggle */}
            <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: '500' }}>Chế độ xem kết quả</span>
                <button 
                  onClick={() => handleToggleShare(!session?.isShared)}
                  style={{
                    width: '100%',
                    padding: '10px 16px',
                    fontSize: '0.85rem',
                    fontWeight: '600',
                    borderRadius: '8px',
                    border: 'none',
                    cursor: 'pointer',
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px',
                    color: '#fff',
                    backgroundColor: session?.isShared ? '#10b981' : '#f59e0b',
                    boxShadow: session?.isShared ? '0 2px 8px rgba(16, 185, 129, 0.2)' : '0 2px 8px rgba(245, 158, 11, 0.2)',
                    transition: 'all 0.2s ease'
                  }}
                  disabled={shareSubmitLoading}
                >
                  {session?.isShared ? (
                    <><Shield size={15} /> Công khai</>
                  ) : (
                    <><ShieldOff size={15} /> Riêng tư</>
                  )}
                </button>
                <small style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '6px', lineHeight: '1.4' }}>
                  {session?.isShared 
                    ? 'Bệnh nhân có thể xem toàn bộ kết luận chẩn đoán trên trang cá nhân.' 
                    : 'Ẩn toàn bộ chẩn đoán và đơn thuốc với tài khoản bệnh nhân.'}
                </small>
              </div>
            </div>

          </div>
        </div>

        {/* RIGHT COLUMN: Interactive Workspaces (Three Core Boxes) */}
        <div style={{ flex: '1', minWidth: '320px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* BOX 1: Triệu chứng lâm sàng */}
          <div className="glass-panel" style={{ padding: '24px', borderRadius: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ padding: '8px', backgroundColor: 'rgba(16, 3, 87, 0.05)', borderRadius: '8px', color: '#100357' }}>
                  <Clipboard size={20} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', margin: 0, fontWeight: '700', color: '#100357' }}>Triệu chứng lâm sàng</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {hasSymptoms ? 'Đã khai báo triệu chứng phụ khoa' : 'Chưa ghi nhận thông số'}
                  </span>
                </div>
              </div>
              <button 
                onClick={() => {
                  setShowSymptoms(!showSymptoms);
                  if (!showSymptoms && hasSymptoms) {
                    setIsEditingSymptoms(false); // Default view is read-only if has data
                  }
                }} 
                className="btn btn-secondary" 
                style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
              >
                {showSymptoms ? (
                  <><ChevronUp size={16} /> Ẩn chi tiết</>
                ) : (
                  <><ChevronDown size={16} /> Xem chi tiết</>
                )}
              </button>
            </div>

            {showSymptoms && (
              <div style={{ marginTop: '20px', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
                
                {/* Header Edit Button */}
                {hasSymptoms && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', padding: '10px 16px', backgroundColor: 'rgba(0,0,0,0.015)', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: '500' }}>
                      {isEditingSymptoms ? 'Đang chỉnh sửa biểu mẫu triệu chứng...' : 'Hồ sơ triệu chứng hiện tại của bệnh nhân'}
                    </span>
                    {!isCompleted && !isEditingSymptoms && (
                      <button 
                        onClick={() => setIsEditingSymptoms(true)} 
                        className="btn btn-primary" 
                        style={{ padding: '6px 12px', fontSize: '0.8rem', backgroundColor: '#100357', borderColor: '#100357' }}
                      >
                        Chỉnh sửa
                      </button>
                    )}
                  </div>
                )}

                {/* Form render based on states */}
                {(!hasSymptoms || isEditingSymptoms) ? (
                  <div>
                    <ClinicalSymptomsForm 
                      initialData={{
                        height: session?.height,
                        weight: session?.weight,
                        menopauseStatus: symptomResult?.menopauseStatus,
                        symptomDuration: symptomResult?.symptomDuration,
                        symptomProgressing: symptomResult?.symptomProgressing,
                        symptomIds: symptomResult?.symptomIds || []
                      }}
                      onSave={handleSymptomsSubmit}
                      sessionId={session?.sessionId}
                      disabled={false}
                    />
                    <button 
                      type="button"
                      onClick={() => {
                        setIsEditingSymptoms(false);
                        if (!hasSymptoms) setShowSymptoms(false);
                      }} 
                      className="btn btn-secondary" 
                      style={{ width: '100%', marginTop: '12px', padding: '10px' }}
                    >
                      Hủy
                    </button>
                  </div>
                ) : (
                  <div>
                    <ClinicalSymptomsForm 
                      initialData={{
                        height: session?.height,
                        weight: session?.weight,
                        menopauseStatus: symptomResult?.menopauseStatus,
                        symptomDuration: symptomResult?.symptomDuration,
                        symptomProgressing: symptomResult?.symptomProgressing,
                        symptomIds: symptomResult?.symptomIds || []
                      }}
                      sessionId={session?.sessionId}
                      disabled={true}
                    />
                  </div>
                )}

              </div>
            )}
          </div>

          {/* BOX 2: Siêu âm & VIA */}
          <div className="glass-panel" style={{ padding: '24px', borderRadius: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ padding: '8px', backgroundColor: 'rgba(16, 3, 87, 0.05)', borderRadius: '8px', color: '#100357' }}>
                  <ImageIcon size={20} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', margin: 0, fontWeight: '700', color: '#100357' }}>Siêu âm & Hình ảnh học</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {ultrasoundScans.length > 0 ? `Đã tải lên ${ultrasoundScans.length} hình ảnh soi CTC` : 'Chưa có dữ liệu hình ảnh'}
                  </span>
                </div>
              </div>
              <button 
                onClick={() => setShowUltrasound(!showUltrasound)} 
                className="btn btn-secondary" 
                style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
              >
                {showUltrasound ? (
                  <><ChevronUp size={16} /> Ẩn chi tiết</>
                ) : (
                  <><ChevronDown size={16} /> Xem chi tiết</>
                )}
              </button>
            </div>

            {showUltrasound && (
              <div style={{ marginTop: '20px', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
                
                {/* Simulated gallery list */}
                {ultrasoundScans.length > 0 ? (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '20px', marginBottom: '24px' }}>
                    {ultrasoundScans.map((scan) => (
                      <div key={scan.id} className="glass-panel" style={{ overflow: 'hidden', border: '1px solid var(--border-color)', borderRadius: '12px', display: 'flex', flexDirection: 'column' }}>
                        <div style={{ position: 'relative', height: '180px', backgroundColor: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          <img 
                            src={scan.url} 
                            alt="Cervical scan" 
                            style={{ width: '100%', height: '100%', objectFit: 'contain' }}
                          />
                          {!isCompleted && (
                            <button 
                              onClick={() => handleDeleteScan(scan.id)}
                              style={{ position: 'absolute', top: '8px', right: '8px', padding: '6px', borderRadius: '50%', border: 'none', backgroundColor: 'rgba(239,68,68,0.85)', color: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                              title="Xóa ảnh này"
                            >
                              <Trash2 size={14} />
                            </button>
                          )}
                        </div>
                        <div style={{ padding: '14px', flex: 1, display: 'flex', flexDirection: 'column', justifyBetween: 'space-between', gap: '10px' }}>
                          <div>
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>
                              Tải lên ngày: {new Date(scan.createdAt).toLocaleDateString('vi-VN')}
                            </span>
                            <p style={{ fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-main)', marginTop: '4px', lineHeight: '1.4' }}>
                              {scan.description}
                            </p>
                          </div>

                          {/* AI Screening status */}
                          <div style={{ borderTop: '1px dashed var(--border-color)', paddingTop: '10px', marginTop: 'auto' }}>
                            {scan.aiClassified ? (
                              <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Phân tích AI:</span>
                                  <span style={{ 
                                    fontSize: '0.75rem', 
                                    fontWeight: '700', 
                                    color: scan.aiClassified.includes('Bình thường') ? '#10b981' : '#dc2626'
                                  }}>
                                    {scan.aiClassified}
                                  </span>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '2px' }}>
                                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Độ tin cậy:</span>
                                  <strong style={{ fontSize: '0.8rem' }}>{scan.confidence}</strong>
                                </div>
                                {scan.details && (
                                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', fontStyle: 'italic', lineHeight: '1.3' }}>
                                    {scan.details}
                                  </p>
                                )}
                              </div>
                            ) : (
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Ảnh chưa được phân tích</span>
                                {!isCompleted && (
                                  <button 
                                    onClick={() => handleRunAiAnalysis(scan.id)}
                                    className="btn btn-secondary"
                                    style={{ width: '100%', padding: '6px 12px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', color: '#100357', borderColor: 'rgba(16,3,87,0.2)' }}
                                    disabled={analyzingScanId !== null}
                                  >
                                    <Cpu size={12} /> Tiến hành quét AI
                                  </button>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div style={{ padding: '24px', textAlign: 'center', border: '1px dashed var(--border-color)', borderRadius: '12px', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px' }}>
                    Chưa có hình ảnh soi tử cung được tải lên.
                  </div>
                )}

                {/* AI Screening loader active */}
                {analyzingScanId && (
                  <div className="glass-panel" style={{ padding: '16px', marginBottom: '24px', backgroundColor: 'rgba(16, 3, 87, 0.02)', border: '1px solid rgba(16, 3, 87, 0.1)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', fontWeight: '600', marginBottom: '6px' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><Cpu size={14} className="pulse-indicator" /> Đang chạy chẩn đoán AI...</span>
                      <span>{analysisProgress}%</span>
                    </div>
                    <div style={{ width: '100%', height: '6px', backgroundColor: 'var(--border-color)', borderRadius: '3px', overflow: 'hidden', marginBottom: '10px' }}>
                      <div style={{ width: `${analysisProgress}%`, height: '100%', backgroundColor: '#100357', transition: 'width 0.3s ease' }} />
                    </div>
                    <div style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: 'var(--text-muted)', display: 'flex', flexDirection: 'column', gap: '4px', textAlign: 'left' }}>
                      {analysisLogs.map((log, index) => (
                        <div key={index}>✓ {log}</div>
                      ))}
                    </div>
                  </div>
                )}

                {/* File Uploader form */}
                {!isCompleted && (
                  <form onSubmit={handleUploadScanSubmit} style={{ borderTop: '1px solid var(--border-color)', paddingTop: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <h4 style={{ fontSize: '0.95rem', fontWeight: '700', color: '#100357', margin: 0 }}>Tải lên hình ảnh soi cổ tử cung mới</h4>
                    
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
                      <div className="form-group" style={{ marginBottom: 0 }}>
                        <label className="form-label" style={{ fontSize: '0.8rem' }}>Mô tả hình ảnh</label>
                        <input 
                          type="text" 
                          className="form-control"
                          placeholder="Ví dụ: VIA dương tính ở góc 3h..."
                          value={scanDesc}
                          onChange={(e) => setScanDesc(e.target.value)}
                        />
                      </div>
                      <div className="form-group" style={{ marginBottom: 0 }}>
                        <label className="form-label" style={{ fontSize: '0.8rem' }}>Chọn tệp ảnh</label>
                        <input 
                          type="file" 
                          accept="image/*"
                          className="form-control"
                          onChange={handleScanFileChange}
                          required
                        />
                      </div>
                    </div>

                    {scanPreviewUrl && (
                      <div style={{ display: 'flex', gap: '12px', alignItems: 'center', padding: '10px', border: '1px solid var(--border-color)', borderRadius: '8px', backgroundColor: 'rgba(0,0,0,0.01)' }}>
                        <img src={scanPreviewUrl} alt="Preview" style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '4px' }} />
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {scanFile?.name} ({(scanFile?.size / 1024).toFixed(1)} KB)
                        </span>
                      </div>
                    )}

                    <button 
                      type="submit" 
                      className="btn btn-primary"
                      style={{ padding: '10px', fontSize: '0.85rem', width: '100%', backgroundColor: '#100357', borderColor: '#100357' }}
                      disabled={uploadingScan}
                    >
                      <Upload size={14} /> {uploadingScan ? `Đang xử lý tải lên (${uploadProgress}%)...` : 'Xác nhận tải lên hình ảnh'}
                    </button>
                  </form>
                )}

              </div>
            )}
          </div>

          {/* BOX 3: Xét nghiệm lâm sàng */}
          <div className="glass-panel" style={{ padding: '24px', borderRadius: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ padding: '8px', backgroundColor: 'rgba(16, 3, 87, 0.05)', borderRadius: '8px', color: '#100357' }}>
                  <FlaskConical size={20} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', margin: 0, fontWeight: '700', color: '#100357' }}>Xét nghiệm lâm sàng</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {labResults.length > 0 ? `Đã có chỉ định ${labResults.length} chỉ mục` : 'Chưa có chỉ định xét nghiệm'}
                  </span>
                </div>
              </div>
              <button 
                onClick={() => setShowLab(!showLab)} 
                className="btn btn-secondary" 
                style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
              >
                {showLab ? (
                  <><ChevronUp size={16} /> Ẩn chi tiết</>
                ) : (
                  <><ChevronDown size={16} /> Xem chi tiết</>
                )}
              </button>
            </div>

            {showLab && (
              <div style={{ marginTop: '20px', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
                
                {/* Lists current labs */}
                {labResults.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '24px' }}>
                    {labResults.map((lab) => {
                      const hasParamsSaved = !!labParams[lab.labResultId];
                      const currentParamValues = labParams[lab.labResultId] || {};
                      const isEditingThisLab = editingLabId === lab.labResultId;

                      return (
                        <div key={lab.labResultId} className="glass-panel" style={{ padding: '16px', border: '1px solid var(--border-color)', borderRadius: '12px', backgroundColor: 'rgba(0,0,0,0.005)' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
                            <div>
                              <strong style={{ fontSize: '0.95rem', color: '#100357', display: 'block' }}>{lab.testType}</strong>
                              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                                Chỉ định lúc: {new Date(lab.createdAt).toLocaleDateString('vi-VN')}
                              </span>
                            </div>
                            <div>
                              {hasParamsSaved ? (
                                <span className="badge badge-completed" style={{ fontSize: '0.7rem' }}>Đã có chỉ số</span>
                              ) : (
                                <span className="badge badge-pending" style={{ fontSize: '0.7rem' }}>Chờ chỉ số</span>
                              )}
                            </div>
                          </div>

                          {/* Render Parameter Details / Inputs */}
                          <div style={{ marginTop: '14px', paddingTop: '12px', borderTop: '1px dashed var(--border-color)' }}>
                            
                            {isEditingThisLab ? (
                              /* Params Entry Form */
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                <span style={{ fontSize: '0.8rem', fontWeight: '600', color: '#100357' }}>Nhập kết quả chỉ số xét nghiệm:</span>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
                                  {(TEST_PARAM_TEMPLATES[lab.testType] || [
                                    { name: 'Kết quả chung', key: 'generalResult', defaultVal: 'Bình thường', unit: '', refRange: 'Bình thường' }
                                  ]).map(p => (
                                    <div key={p.key} className="form-group" style={{ marginBottom: 0 }}>
                                      <label className="form-label" style={{ fontSize: '0.75rem' }}>
                                        {p.name} {p.unit ? `(${p.unit})` : ''}
                                      </label>
                                      <input 
                                        type="text" 
                                        className="form-control"
                                        style={{ padding: '6px 10px', fontSize: '0.85rem' }}
                                        value={labInputParams[p.key] ?? ''}
                                        onChange={(e) => setLabInputParams({
                                          ...labInputParams,
                                          [p.key]: e.target.value
                                        })}
                                      />
                                    </div>
                                  ))}
                                </div>
                                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '6px' }}>
                                  <button type="button" onClick={() => setEditingLabId(null)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.75rem' }}>
                                    Hủy
                                  </button>
                                  <button type="button" onClick={() => handleSaveParams(lab.labResultId)} className="btn btn-primary" style={{ padding: '6px 16px', fontSize: '0.75rem', backgroundColor: '#10b981', borderColor: '#10b981' }}>
                                    Lưu chỉ số
                                  </button>
                                </div>
                              </div>
                            ) : (
                              /* Params Display view */
                              <div>
                                {hasParamsSaved ? (
                                  <div>
                                    <div className="custom-table-container" style={{ borderRadius: '8px', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
                                      <table className="custom-table" style={{ fontSize: '0.8rem' }}>
                                        <thead>
                                          <tr style={{ backgroundColor: 'rgba(0,0,0,0.01)' }}>
                                            <th style={{ padding: '8px 12px' }}>Chỉ số</th>
                                            <th style={{ padding: '8px 12px' }}>Giá trị</th>
                                            <th style={{ padding: '8px 12px' }}>Tham chiếu</th>
                                            <th style={{ padding: '8px 12px' }}>Đơn vị</th>
                                          </tr>
                                        </thead>
                                        <tbody>
                                          {(TEST_PARAM_TEMPLATES[lab.testType] || [
                                            { name: 'Kết quả chung', key: 'generalResult', defaultVal: 'Bình thường', unit: '', refRange: 'Bình thường' }
                                          ]).map(p => (
                                            <tr key={p.key}>
                                              <td style={{ padding: '8px 12px', fontWeight: '500' }}>{p.name}</td>
                                              <td style={{ padding: '8px 12px', fontWeight: '700', color: '#100357' }}>
                                                {currentParamValues[p.key] || 'N/A'}
                                              </td>
                                              <td style={{ padding: '8px 12px', color: 'var(--text-muted)' }}>{p.refRange}</td>
                                              <td style={{ padding: '8px 12px', color: 'var(--text-muted)' }}>{p.unit || '-'}</td>
                                            </tr>
                                          ))}
                                        </tbody>
                                      </table>
                                    </div>
                                    {!isCompleted && (
                                      <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '10px' }}>
                                        <button 
                                          type="button" 
                                          onClick={() => handleEditParamsClick(lab)} 
                                          className="btn btn-secondary"
                                          style={{ padding: '4px 10px', fontSize: '0.7rem' }}
                                        >
                                          Chỉnh sửa chỉ số
                                        </button>
                                      </div>
                                    )}
                                  </div>
                                ) : (
                                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Chưa cập nhật chỉ số đo lường kết quả.</span>
                                    {!isCompleted && (
                                      <button 
                                        type="button" 
                                        onClick={() => handleEditParamsClick(lab)} 
                                        className="btn btn-secondary"
                                        style={{ padding: '6px 12px', fontSize: '0.75rem', color: '#100357', borderColor: 'rgba(16,3,87,0.2)' }}
                                      >
                                        Nhập chỉ số
                                      </button>
                                    )}
                                  </div>
                                )}
                              </div>
                            )}

                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', border: '1px dashed var(--border-color)', borderRadius: '12px', marginBottom: '24px' }}>
                    Chưa có chỉ định xét nghiệm nào cho phiên khám này.
                  </div>
                )}

                {/* Create new lab test */}
                {!isCompleted && (
                  <form onSubmit={handleAddLabResult} style={{ borderTop: '1px solid var(--border-color)', paddingTop: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" style={{ fontWeight: '600', fontSize: '0.85rem', color: '#100357' }}>Chỉ định xét nghiệm bổ sung</label>
                      <select 
                        className="form-control"
                        value={newLabType}
                        onChange={(e) => setNewLabType(e.target.value)}
                      >
                        <option value="Xét nghiệm máu tổng quát">Xét nghiệm máu tổng quát</option>
                        <option value="Xét nghiệm Pap Smear">Xét nghiệm Pap Smear</option>
                        <option value="Xét nghiệm HPV DNA">Xét nghiệm HPV DNA</option>
                        <option value="Soi cổ tử cung (Colposcopy)">Soi cổ tử cung (Colposcopy)</option>
                        <option value="Sinh thiết cổ tử cung (Biopsy)">Sinh thiết cổ tử cung (Biopsy)</option>
                      </select>
                    </div>
                    
                    <button type="submit" className="btn btn-secondary" style={{ width: '100%', fontSize: '0.85rem', padding: '10px' }} disabled={labSubmitLoading}>
                      <PlusCircle size={14} /> {labSubmitLoading ? 'Đang gửi...' : 'Gửi yêu cầu xét nghiệm'}
                    </button>
                  </form>
                )}

              </div>
            )}
          </div>

          {/* BOX 4: Chẩn đoán & Đơn thuốc điều trị */}
          <div className="glass-panel" style={{ padding: '24px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
            <h2 style={{ fontSize: '1.2rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px', color: '#100357', fontWeight: '700' }}>
              <CheckCircle size={20} />
              Chẩn đoán & Đơn thuốc điều trị
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '16px', lineHeight: '1.4' }}>
              Sau khi thu thập đầy đủ triệu chứng phụ khoa, hình ảnh soi và chỉ số xét nghiệm, bác sĩ nhập kết luận chẩn đoán cuối cùng và phác đồ điều trị cho bệnh nhân.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label" style={{ fontWeight: '600' }}>Kết luận chẩn đoán</label>
                {isCompleted ? (
                  <div style={{ padding: '12px 16px', backgroundColor: 'rgba(0,0,0,0.015)', border: '1px solid var(--border-color)', borderRadius: '8px', fontSize: '0.95rem', fontWeight: '700', color: '#100357' }}>
                    {diagnosisConclusion || 'Theo dõi viêm cổ tử cung nhẹ / SCJ loại 1'}
                  </div>
                ) : (
                  <textarea 
                    className="form-control"
                    rows="2"
                    placeholder="Nhập kết luận lâm sàng tổng quan..."
                    value={diagnosisConclusion}
                    onChange={(e) => setDiagnosisConclusion(e.target.value)}
                  />
                )}
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label" style={{ fontWeight: '600' }}>Kế hoạch điều trị & Đơn thuốc</label>
                {isCompleted ? (
                  <div style={{ padding: '12px 16px', backgroundColor: 'rgba(0,0,0,0.015)', border: '1px solid var(--border-color)', borderRadius: '8px', fontSize: '0.9rem', color: 'var(--text-main)', lineHeight: '1.5', whiteSpace: 'pre-line' }}>
                    {treatmentPlan || 'Kế hoạch điều trị:\n- Bổ sung Cephalosporin đường uống\n- Hẹn khám lại sau 3 tháng.'}
                  </div>
                ) : (
                  <textarea 
                    className="form-control"
                    rows="3"
                    placeholder="Nhập kế hoạch điều trị, lời khuyên y tế, đơn thuốc hỗ trợ..."
                    value={treatmentPlan}
                    onChange={(e) => setTreatmentPlan(e.target.value)}
                  />
                )}
              </div>

            </div>

            {!isCompleted ? (
              <button 
                onClick={handleCompleteSession} 
                className="btn btn-primary" 
                style={{ width: '100%', marginTop: '20px', padding: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', backgroundColor: '#10b981', borderColor: '#10b981', boxShadow: '0 4px 12px rgba(16,185,129,0.2)' }}
                disabled={statusSubmitLoading}
              >
                <Check size={18} /> {statusSubmitLoading ? 'Đang hoàn tất...' : 'Xác nhận & Hoàn thành bệnh án'}
              </button>
            ) : (
              <div style={{ marginTop: '20px', textAlign: 'center', color: '#10b981', fontWeight: '700', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', padding: '12px', borderRadius: '8px', backgroundColor: 'rgba(16,185,129,0.08)' }}>
                <CheckCircle size={18} /> Phiên khám bệnh án đã hoàn tất chẩn đoán.
              </div>
            )}
          </div>

        </div>

      </div>
    </div>
  );
}

