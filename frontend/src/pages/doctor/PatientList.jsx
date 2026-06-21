import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import MainLayout from '../../layouts/MainLayout';
import StatusBadge from '../../components/StatusBadge';
import './PatientList.css';

// Mock data – replace with API call when session endpoint is available
const MOCK_SESSIONS = [
    { sessionId: 1, patientName: 'Phạm Thùy Linh',    patientId: 1, weight: 52, height: 155, status: 'COMPLETED', isShared: false, createdAt: '2026-01-10T09:00:00' },
    { sessionId: 2, patientName: 'Lê Minh Hoàng',      patientId: 2, weight: 58, height: 160, status: 'COMPLETED', isShared: true,  createdAt: '2026-02-15T14:30:00' },
    { sessionId: 3, patientName: 'Nguyễn Thu Hương',   patientId: 3, weight: 61, height: 158, status: 'PENDING',   isShared: false, createdAt: '2026-03-20T11:00:00' },
    { sessionId: 5, patientName: 'Đỗ Thanh Mỹ',        patientId: 5, weight: 67, height: 162, status: 'PENDING',   isShared: true,  createdAt: '2026-05-20T08:45:00' },
];

export default function PatientList() {
    const navigate = useNavigate();
    const [sessions,  setSessions]  = useState([]);
    const [search,    setSearch]    = useState('');
    const [filter,    setFilter]    = useState('ALL');
    const [loading,   setLoading]   = useState(true);

    useEffect(() => {
        // Simulate API loading delay
        const t = setTimeout(() => { setSessions(MOCK_SESSIONS); setLoading(false); }, 400);
        return () => clearTimeout(t);
    }, []);

    const filtered = sessions.filter(s => {
        const matchSearch = s.patientName.toLowerCase().includes(search.toLowerCase())
            || String(s.sessionId).includes(search);
        const matchFilter = filter === 'ALL' || s.status === filter;
        return matchSearch && matchFilter;
    });

    return (
        <MainLayout>
            <div className="page-header">
                <h1>Patient List</h1>
                <p className="text-muted">Manage diagnosis sessions assigned to you</p>
            </div>

            {/* Filter bar */}
            <div className="patient-list-toolbar card" style={{ padding: '16px 20px', marginBottom: 20 }}>
                <input
                    className="form-input"
                    style={{ maxWidth: 300 }}
                    placeholder="🔍  Search by name or session ID…"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <div className="filter-tabs">
                    {['ALL', 'PENDING', 'COMPLETED'].map(f => (
                        <button
                            key={f}
                            className={`filter-tab ${filter === f ? 'active' : ''}`}
                            onClick={() => setFilter(f)}
                        >
                            {f}
                        </button>
                    ))}
                </div>
            </div>

            {/* Table */}
            {loading ? (
                <div style={{ textAlign: 'center', padding: 64 }}>
                    <span className="spinner spinner-dark" />
                </div>
            ) : filtered.length === 0 ? (
                <div className="empty-state card">
                    <span style={{ fontSize: '2.5rem' }}>👥</span>
                    <p>No sessions found{search ? ` for "${search}"` : ''}.</p>
                </div>
            ) : (
                <div className="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>Session</th>
                            <th>Patient</th>
                            <th>Weight / Height</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Shared</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filtered.map(s => (
                            <tr key={s.sessionId}>
                                <td className="mono fw-600 text-primary">#{s.sessionId}</td>
                                <td>
                                    <div style={{ fontWeight: 600 }}>{s.patientName}</div>
                                    <div className="text-muted" style={{ fontSize: '0.8rem' }}>ID: {s.patientId}</div>
                                </td>
                                <td className="mono">{s.weight} kg / {s.height} cm</td>
                                <td className="text-muted" style={{ fontSize: '0.875rem' }}>
                                    {new Date(s.createdAt).toLocaleDateString('vi-VN')}
                                </td>
                                <td><StatusBadge status={s.status} /></td>
                                <td>
                    <span className={`share-pill ${s.isShared ? 'shared' : 'private'}`}>
                      {s.isShared ? '🔓 Shared' : '🔒 Private'}
                    </span>
                                </td>
                                <td>
                                    <button
                                        className="btn btn-primary btn-sm"
                                        onClick={() => navigate(`/doctor/diagnosis/${s.sessionId}`)}
                                    >
                                        Open
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </MainLayout>
    );
}
