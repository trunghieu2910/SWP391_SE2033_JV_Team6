import { useAuth } from '../../context/AuthContext';
import MainLayout from '../../layouts/MainLayout';

export default function AdminDashboard() {
    const { user } = useAuth();
    return (
        <MainLayout>
            <div className="page-header">
                <h1>Admin Dashboard</h1>
                <p className="text-muted">Logged in as {user?.fullName}</p>
            </div>
            <div className="card">
                <p className="text-muted">System overview coming soon.</p>
            </div>
        </MainLayout>
    );
}
