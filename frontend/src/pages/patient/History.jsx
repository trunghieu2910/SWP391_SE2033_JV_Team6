import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import labResultService from '../../services/labResultService';
import StatusBadge from '../../components/StatusBadge';
import MainLayout from '../../layouts/MainLayout';
import './History.css';
import { useToast, ToastContainer } from '../../components/Toast';
import './History.css';

// ── Mock sessions for the logged-in patient until session API is wired
const MOCK_SESSIONS = [
    { sessionId: 1, doctorName: 'Dr. Nguyễn Văn Tùng', createdAt: '2026-01-10', status: 'COMPLETED', isShared: true,  weight: 52, height: 155 },
    { sessionId: 2, doctorName: 'Dr. Trần Thị Mai',    createdAt: '2026-02-15', status: 'COMPLETED', isShared: true,   weight: 58, height: 160 },
    { sessionId: 5, doctorName: 'Dr. Nguyễn Văn Tùng', createdAt: '2026-05-20', status: 'PENDING',   isShared: true,   weight: 67, height: 162 },
];

function LabResultDetail({ sessionId, onClose }) {
    const [results,  setResults]  = useState([]);
    const [loading,  setLoading]  = useState(true);
    const [error,    setError]    = useState(null);

    useEffect(() => {
        setLoading(true); setError(null);
        labResultService.getBySession(sessionId)
            .then(res => setResults(res.data?.data ?? res.data ?? []))
            .catch(err => {
                if (err.response?.status === 403) setError('access_denied');
                else if (err.response?.status === 404) setError('not_found');
                else setError('generic');
            })
            .finally(() => setLoading(false));
    }, [sessionId]);

    if (loading) return (
        <div style={{ textAlign: 'center', padding: 48 }}>
            <span className="spinner spinner-dark" />
        </div>
    );

    if (error === 'access_denied') return (
        <div className="access-denied">
            <div className="access-denied-icon">🔒</div>
            <h4>Access Denied</h4>
            <p className="text-muted">These results have not been shared with you yet. Please contact your doctor for access.</p>
            <button className="btn btn-outline mt-16" onClick={onClose}>Go Back</button>
        </div>
    );

    if (error) return (
        <div className="access-denied">
            <div className="access-denied-icon">⚠️</div>
            <h4>Unable to Load Results</h4>
            <p className="text-muted">Something went wrong. Please try again later.</p>
            <button className="btn btn-outline mt-16" onClick={onClose}>Go Back</button>
        </div>
    );

    if (results.length === 0) return (
        <div className="empty-state">
            <span style={{ fontSize: '3rem' }}>🧪</span>
            <p>No lab tests have been recorded for this session yet.</p>
            <button className="btn btn-ghost mt-16" onClick={onClose}>← Back to History</button>
        </div>
    );

    return (
        <div>
            <button className="btn btn-ghost mb-16" onClick={onClose}>← Back to History</button>
            <div className="lab-results-detail">
                {results.map((r, i) => (
                    <div key={r.labResultId ?? i} className="lab-detail-card card">
                        <div className="lab-detail-header">
                            <span className="lab-detail-icon">🧪</span>
                            <div>
                                <div className="lab-detail-type"># {r.labResultId}. {r.testType}</div>
                                <div className="lab-detail-date text-muted">
                                    {r.createdAt ? new Date(r.createdAt).toLocaleString('vi-VN') : '—'}
                                </div>
                            </div>
                            <StatusBadge status={r.status} />
                        </div>

                        {r.parameters?.length > 0 ? (
                            <div className="table-wrapper mt-16">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Parameter</th>
                                        <th>Value</th>
                                        <th>Unit</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {r.parameters.map((p, j) => (
                                        <tr key={j}>
                                            <td>{p.parameterName ?? p.name}</td>
                                            <td className="mono fw-600">{p.value}</td>
                                            <td className="text-muted">{p.unit}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        ) : (
                            <p className="text-muted mt-16" style={{ fontSize: '0.875rem' }}>No parameters recorded.</p>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

export default function History() {
    const [sessions, setSessions] = useState(MOCK_SESSIONS);
    const [selected, setSelected] = useState(null); // sessionId being viewed
    const { toasts, addToast, removeToast } = useToast();

    const handleView = (session) => {
        if (!session.isShared) {
            addToast('These results have not been shared by your doctor yet.', 'warning');
            return;
        }
        setSelected(session.sessionId);
    };

    if (selected !== null) {
        return (
            <MainLayout>
                <div className="page-header">
                    <h1>Lab Results</h1>
                    <p className="text-muted">Session #{selected}</p>
                </div>
                <LabResultDetail sessionId={selected} onClose={() => setSelected(null)} />
                <ToastContainer toasts={toasts} onRemove={removeToast} />
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <div className="page-header">
                <h1>My Medical History</h1>
                <p className="text-muted">View your diagnosis sessions and lab test results</p>
            </div>

            {sessions.length === 0 ? (
                <div className="empty-state card">
                    <span style={{ fontSize: '3rem' }}>📋</span>
                    <p>No diagnosis sessions found.</p>
                </div>
            ) : (
                <div className="sessions-list">
                    {sessions.map(s => (
                        <div key={s.sessionId} className="session-card card">
                            <div className="session-card-top">
                                <div className="session-card-id">Session #{s.sessionId}</div>
                                <StatusBadge status={s.status} />
                            </div>

                            <div className="session-card-info">
                                <div className="session-info-item">
                                    <span className="session-info-label">Doctor</span>
                                    <span className="session-info-value">{s.doctorName}</span>
                                </div>
                                <div className="session-info-item">
                                    <span className="session-info-label">Date</span>
                                    <span className="session-info-value">
                    {new Date(s.createdAt).toLocaleDateString('vi-VN')}
                  </span>
                                </div>
                                <div className="session-info-item">
                                    <span className="session-info-label">Weight / Height</span>
                                    <span className="session-info-value mono">{s.weight} kg / {s.height} cm</span>
                                </div>
                            </div>

                            <hr className="divider" />

                            <div className="session-card-footer">
                                {s.isShared ? (
                                    <button
                                        className="btn btn-primary btn-sm"
                                        onClick={() => handleView(s)}
                                    >
                                        📋 View Lab Results
                                    </button>
                                ) : (
                                    <div className="not-shared-msg">
                                        <span>🔒</span>
                                        <span className="text-muted">Results not yet shared by your doctor</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <ToastContainer toasts={toasts} onRemove={removeToast} />
        </MainLayout>
    );
}
