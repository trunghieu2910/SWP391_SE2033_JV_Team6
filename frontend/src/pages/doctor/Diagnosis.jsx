import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import labResultService from '../../services/labResultService';
import diagnosisSessionService from '../../services/diagnosisSessionService';
import lisIntegrationService from '../../services/lisIntegrationService';
import StatusBadge from '../../components/StatusBadge';
import MainLayout from '../../layouts/MainLayout';
import './Diagnosis.css';
import { useToast, ToastContainer } from '../../components/Toast';

// Danh sách loại xét nghiệm — giữ fix cứng theo yêu cầu
const TEST_TYPES = [
    'Xét nghiệm tế bào học cổ tử cung',
    'Xét nghiệm DNA của virus HPV',
    'Định tuýp HPV nguy cơ cao',
    'Sinh thiết',
    'Dấu ấn ung thư SCC',
    'Xét nghiệm máu cơ bản',
];

// Kho dữ liệu mô phỏng kết quả trả về từ máy LIS
const LIS_MOCK_DATA = {
    'Xét nghiệm tế bào học cổ tử cung': [
        { testName: 'Kết quả tế bào học', resultValue: 'HSIL', unit: null },
        { testName: 'Mức độ viêm', resultValue: 'Trung bình', unit: null }
    ],
    'Xét nghiệm DNA của virus HPV': [
        { testName: 'HPV DNA Test', resultValue: 'Positive', unit: null }
    ],
    'Định tuýp HPV nguy cơ cao': [
        { testName: 'Tuýp 16', resultValue: 'Positive', unit: null },
        { testName: 'Tuýp 18', resultValue: 'Negative', unit: null },
        { testName: '12 Tuýp nguy cơ cao khác', resultValue: 'Negative', unit: null }
    ],
    'Sinh thiết': [
        { testName: 'Kết quả giải phẫu bệnh', resultValue: 'CIN 2', unit: null },
        { testName: 'Mô tả', resultValue: 'Tổn thương tiền ung thư mức độ trung bình', unit: null }
    ],
    'Dấu ấn ung thư SCC': [
        { testName: 'Chỉ số SCC Antigen', resultValue: '2.8', unit: 'ng/mL' }
    ],
    'Xét nghiệm máu cơ bản': [
        { testName: 'Hồng cầu (RBC)', resultValue: '4.2', unit: 'T/L' },
        { testName: 'Bạch cầu (WBC)', resultValue: '6.5', unit: 'G/L' },
        { testName: 'Tiểu cầu (PLT)', resultValue: '250', unit: 'G/L' }
    ]
};

function LabResultCard({ result, onSimulate, isSimulating }) {
    return (
        <div className="lab-card open">
            {/* Thêm display: flex để căn chỉnh nút bấm sang bên phải */}
            <div className="lab-card-header lab-card-header--static" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span className="lab-card-icon">🧪</span>
                <span className="lab-card-type">#{result.labResultId}. {result.testType}</span>
                <span className="lab-card-date text-muted">
                    {result.createdAt
                        ? new Date(result.createdAt).toLocaleString('vi-VN')
                        : '—'}
                </span>
                <StatusBadge status={result.status} />

                {/* THÊM NÚT BẤM NÀY: Hiện nút nếu trạng thái đang là PENDING */}
                {result.status === 'PENDING' && (
                    <button
                        type="button"
                        className="btn btn-sm btn-primary"
                        style={{ marginLeft: 'auto' }}
                        disabled={isSimulating}
                        onClick={() => onSimulate(result.labResultId, result.testType)}
                    >
                        {isSimulating ? 'Đang chạy máy...' : 'Lấy kết quả LIS'}
                    </button>
                )}
            </div>

            <div className="lab-card-body">
                {result.parameters?.length ? (
                    <div className="table-wrapper">
                        {/* Bảng dữ liệu giữ nguyên */}
                        <table>
                            <thead>
                            <tr>
                                <th>PARAMETER</th>
                                <th>VALUE</th>
                                <th>UNIT</th>
                            </tr>
                            </thead>
                            <tbody>
                            {result.parameters.map((p, i) => (
                                <tr key={i}>
                                    <td>{p.parameterName ?? p.name}</td>
                                    <td className="mono fw-600">{p.value}</td>
                                    <td className="text-muted">{p.unit ?? '—'}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p className="text-muted" style={{ padding: '16px', fontSize: '0.9rem' }}>
                        Chưa có thông số nào được ghi nhận. Đang chờ kết quả từ phòng xét nghiệm.
                    </p>
                )}
            </div>
        </div>
    );
}

export default function Diagnosis() {
    const { sessionId } = useParams();
    const navigate = useNavigate();
    const { toasts, addToast, removeToast } = useToast();

    const effectiveSessionId = sessionId ?? '1';

    const [session, setSession] = useState(null);
    const [labResults, setLabResults] = useState([]);
    const [loading, setLoading] = useState({ session: true, results: true });

    const [formOpen, setFormOpen] = useState(false);
    const [testType, setTestType] = useState('');
    const [formErrors, setFormErrors] = useState({});
    const [apiError, setApiError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const [simulatingId, setSimulatingId] = useState(null);

    useEffect(() => {
        setLoading(prev => ({ ...prev, session: true }));
        diagnosisSessionService.getById(effectiveSessionId)
            .then(res => {
                const data = res.data?.data ?? res.data;
                setSession(data);
            })
            .catch(err => {
                if (err.response?.status === 404) setApiError('notfound');
                else if (err.response?.status === 403) setApiError('forbidden');
            })
            .finally(() => setLoading(prev => ({ ...prev, session: false })));
    }, [effectiveSessionId]);

    useEffect(() => {
        setLoading(prev => ({ ...prev, results: true }));
        labResultService.getBySession(effectiveSessionId)
            .then(res => setLabResults(res.data?.data ?? res.data ?? []))
            .catch(err => {
                if (err.response?.status !== 403 && err.response?.status !== 404) {
                    addToast('Không thể tải danh sách xét nghiệm.', 'error');
                }
            })
            .finally(() => setLoading(prev => ({ ...prev, results: false })));
    }, [effectiveSessionId]);

    const handleCancel = () => {
        setTestType('');
        setFormErrors({});
        setApiError('');
        setFormOpen(false);
    };

    const validate = () => {
        const errs = {};
        if (!testType) errs.testType = 'Vui lòng chọn loại xét nghiệm';
        return errs;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setApiError('');
        const errs = validate();
        if (Object.keys(errs).length) { setFormErrors(errs); return; }

        setSubmitting(true);
        try {
            const payload = { sessionId: Number(effectiveSessionId), testType };
            const res = await labResultService.createLabResult(payload);
            const newResult = res.data?.data ?? res.data;
            setLabResults(prev => [newResult, ...prev]);
            setTestType('');
            setFormErrors({});
            setFormOpen(false);
            addToast('Tạo xét nghiệm thành công!', 'success');
        } catch (err) {
            const status = err.response?.status;
            const msg = err.response?.data?.message ?? '';
            if (status === 403) setApiError('unauthorized');
            else if (status === 400 && msg.toLowerCase().includes('complet')) setApiError('completed');
            else if (status === 404) setApiError('session_notfound');
            else addToast('Tạo xét nghiệm thất bại. Vui lòng thử lại.', 'error');
        } finally {
            setSubmitting(false);
        }
    };

    const handleSimulateResult = async (labResultId, testType) => {
        setSimulatingId(labResultId);

        // 1. Tự động nhặt bộ kết quả tương ứng từ Kho dữ liệu
        // Nếu không khớp tên thì lấy một mảng rỗng để tránh lỗi
        const mockResults = LIS_MOCK_DATA[testType] || [];

        // 2. Đóng gói dữ liệu gửi lên API giả lập LIS
        const payload = {
            labResultId: labResultId,
            testResults: mockResults
        };

        try {
            const res = await lisIntegrationService.sendResults(payload);
            const updatedResult = res.data?.data ?? res.data;

            // Cập nhật UI
            setLabResults(prev => prev.map(item =>
                item.labResultId === labResultId ? { ...item, ...updatedResult } : item
            ));

            addToast('Đã nhận kết quả xét nghiệm thành công!', 'success');
        } catch (err) {
            const msg = err.response?.data?.message ?? 'Lỗi khi kết nối với máy xét nghiệm.';
            addToast(msg, 'error');
        } finally {
            setSimulatingId(null);
        }
    };

    if (apiError === 'forbidden') return (
        <MainLayout>
            <div className="error-screen card">
                <div className="error-screen-icon">🚫</div>
                <h3>Không có quyền truy cập</h3>
                <p className="text-muted">Bạn không được phép xem xét nghiệm của phiên này.</p>
                <button className="btn btn-outline mt-24" onClick={() => navigate(-1)}>Quay lại</button>
            </div>
        </MainLayout>
    );

    if (apiError === 'notfound') return (
        <MainLayout>
            <div className="error-screen card">
                <div className="error-screen-icon">🔍</div>
                <h3>Không tìm thấy phiên khám</h3>
                <p className="text-muted">Phiên #{effectiveSessionId} không tồn tại.</p>
                <button className="btn btn-outline mt-24" onClick={() => navigate('/doctor/patients')}>
                    Về danh sách bệnh nhân
                </button>
            </div>
        </MainLayout>
    );

    const isCompleted = session?.status === 'COMPLETED';
    const isPageLoading = loading.session;

    return (
        <MainLayout>
            <div className="page-header">
                <h1>Diagnosis Review</h1>
                <p className="text-muted">Manage lab tests for Session #{effectiveSessionId}</p>
            </div>

            {loading.session ? (
                <div className="session-banner" style={{ opacity: 0.5 }}>
                    <div className="session-banner-grid">
                        {['PATIENT', 'SESSION ID', 'STATUS'].map(label => (
                            <div key={label}>
                                <div className="banner-label">{label}</div>
                                <div className="banner-value skeleton-text">—</div>
                            </div>
                        ))}
                    </div>
                </div>
            ) : session && (
                <div className={`session-banner ${isCompleted ? 'completed' : ''}`}>
                    <div className="session-banner-grid">
                        <div>
                            <div className="banner-label">PATIENT</div>
                            <div className="banner-value">{session.patientName}</div>
                        </div>
                        <div>
                            <div className="banner-label">SESSION ID</div>
                            <div className="banner-value mono">#{session.sessionId}</div>
                        </div>
                        <div>
                            <div className="banner-label">STATUS</div>
                            <div className="banner-value">
                                <StatusBadge status={session.status} />
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {isCompleted && (
                <div className="alert alert-warning mb-24">
                    <span>⚠️</span>
                    <span>Phiên này đã hoàn thành. Không thể thêm xét nghiệm mới.</span>
                </div>
            )}

            {!isCompleted && (
                <div className="card mb-24">
                    <div className="new-lab-header">
                        <h3>New Lab Test Request</h3>
                        {!isCompleted && (
                            <button
                                type="button"
                                className={`btn-toggle-form ${formOpen ? 'open' : ''}`}
                                onClick={() => setFormOpen(prev => !prev)}
                                title={formOpen ? 'Thu gọn' : 'Thêm xét nghiệm mới'}
                                aria-expanded={formOpen}
                            >
                                {formOpen ? '×' : '+'}
                            </button>
                        )}
                    </div>

                    {formOpen && (
                        <div className="new-lab-form">
                            {apiError === 'unauthorized' && (
                                <div className="alert alert-error mb-16">
                                    <span>🚫</span>
                                    <span>Bạn không có quyền thêm xét nghiệm vào phiên này.</span>
                                </div>
                            )}
                            {apiError === 'completed' && (
                                <div className="alert alert-error mb-16">
                                    <span>⚠️</span>
                                    <span>Phiên này đã hoàn thành. Không thể thêm xét nghiệm mới.</span>
                                </div>
                            )}
                            {formErrors.global && (
                                <div className="alert alert-error mb-16">
                                    <span>⚠️</span>
                                    <span>{formErrors.global}</span>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} noValidate>
                                <fieldset disabled={submitting || isPageLoading} style={{ border: 'none', padding: 0 }}>

                                    <div className="form-group">
                                        <label className="form-label" htmlFor="testType">
                                            Test Type <span className="required">*</span>
                                        </label>
                                        <select
                                            id="testType"
                                            className={`form-input ${formErrors.testType ? 'error' : ''}`}
                                            value={testType}
                                            onChange={e => {
                                                setTestType(e.target.value);
                                                setFormErrors(p => ({ ...p, testType: '' }));
                                            }}
                                        >
                                            <option value="">— Chọn loại xét nghiệm —</option>
                                            {TEST_TYPES.map(t => (
                                                <option key={t} value={t}>{t}</option>
                                            ))}
                                        </select>
                                        {formErrors.testType && (
                                            <span className="form-error">⚠ {formErrors.testType}</span>
                                        )}
                                    </div>

                                    <div className="form-actions mt-24">
                                        <button type="button" className="btn btn-outline" onClick={handleCancel}>
                                            Cancel
                                        </button>
                                        <button type="submit" className="btn btn-primary" disabled={submitting}>
                                            {submitting && <span className="spinner" />}
                                            {submitting ? 'Submitting…' : 'Submit Lab Test Request'}
                                        </button>
                                    </div>
                                </fieldset>
                            </form>
                        </div>
                    )}
                </div>
            )}

            <div>
                <h3 style={{ marginBottom: 16 }}>Previous Lab Tests for This Session</h3>
                {loading.results ? (
                    <div style={{ textAlign: 'center', padding: 40 }}>
                        <span className="spinner spinner-dark" />
                    </div>
                ) : labResults.length === 0 ? (
                    <div className="empty-state card">
                        <span style={{ fontSize: '3rem' }}>🧪</span>
                        <p>Chưa có xét nghiệm nào cho phiên này.</p>
                    </div>
                ) : (
                    <div className="lab-results-list">
                        {labResults.map((r, i) => (
                            <LabResultCard
                                key={r.labResultId ?? i}
                                result={r}
                                onSimulate={handleSimulateResult}
                                isSimulating={simulatingId === r.labResultId}
                            />
                        ))}
                    </div>
                )}
            </div>

            <ToastContainer toasts={toasts} onRemove={removeToast} />
        </MainLayout>
    );
}
