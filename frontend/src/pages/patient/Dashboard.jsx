import { useAuth } from '../../context/AuthContext';
import MainLayout from '../../layouts/MainLayout';
import { useNavigate } from 'react-router-dom';

export default function PatientDashboard() {
    const { user }   = useAuth();
    const navigate   = useNavigate();

    return (
        <MainLayout>
            <div className="page-header">
                <h1>Welcome, {user?.fullName ?? 'Patient'} 👋</h1>
                <p className="text-muted">Here's a summary of your health journey</p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px,1fr))', gap: 16, marginBottom: 32 }}>
                {[
                    { icon: '📋', label: 'Total Sessions',  value: '3', color: '#EBF2FF', tc: '#1B4F9B' },
                    { icon: '🧪', label: 'Lab Tests Done',  value: '2', color: '#E0F5F5', tc: '#0A9396' },
                    { icon: '🔓', label: 'Shared Results',  value: '2', color: '#E8F5E9', tc: '#2E7D32' },
                ].map(c => (
                    <div key={c.label} className="card" style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                        <div style={{ width: 52, height: 52, borderRadius: 12, background: c.color, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.6rem', flexShrink: 0 }}>
                            {c.icon}
                        </div>
                        <div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: c.tc }}>{c.value}</div>
                            <div style={{ fontSize: '0.8125rem', color: 'var(--neutral-500)', fontWeight: 500 }}>{c.label}</div>
                        </div>
                    </div>
                ))}
            </div>

            <div className="card">
                <h3 style={{ marginBottom: 16 }}>Quick Actions</h3>
                <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
                    <button className="btn btn-primary" onClick={() => navigate('/patient/history')}>
                        📋 View My History
                    </button>
                    <button className="btn btn-outline" onClick={() => navigate('/patient/upload')}>
                        🖼️ Upload Image
                    </button>
                </div>
            </div>
        </MainLayout>
    );
}
