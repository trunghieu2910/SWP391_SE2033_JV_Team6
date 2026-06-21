import Sidebar from '../components/Sidebar';
import Navbar  from '../components/Navbar';
import { ToastContainer, useToast } from '../components/Toast';

export default function MainLayout({ children }) {
    const { toasts, addToast, removeToast } = useToast();

    return (
        <div className="app-shell">
            <Sidebar />
            <div className="main-content">
                <Navbar onLogoutSuccess={() => addToast('Signed out successfully', 'success')} />
                <main className="page-body">
                    {children}
                </main>
            </div>
            <ToastContainer toasts={toasts} onRemove={removeToast} />
        </div>
    );
}
