import { Outlet } from 'react-router';
import Sidebar from '../components/admin/layout/Sidebar';
import { Toaster } from '../components/ui/sonner';

export default function DashboardLayout() {
    return (
        <div className="flex min-h-screen bg-white">
            <Sidebar />

            <main className="flex-1 overflow-auto" style={{ marginLeft: '260px' }}>
                <Outlet />
            </main>

            <Toaster position="top-right" />
        </div>
    );
}