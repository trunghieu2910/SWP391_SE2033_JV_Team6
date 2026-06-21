import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Auth pages
import Login          from '../pages/auth/Login';
import ForgotPassword from '../pages/auth/ForgotPassword';

// Doctor pages
import PatientList  from '../pages/doctor/PatientList';
import Diagnosis    from '../pages/doctor/Diagnosis';
// Patient pages
import PatientDashboard from '../pages/patient/Dashboard';
import History          from '../pages/patient/History';

// Admin pages
import AdminDashboard from '../pages/admin/Dashboard';

// ── Guard: must be authenticated ──────────────────────────────
function RequireAuth({ children }) {
    const { isAuthenticated, isLoading } = useAuth();
    if (isLoading) return <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:'100vh' }}><span className="spinner spinner-dark" /></div>;
    return isAuthenticated ? children : <Navigate to="/login" replace />;
}

// ── Guard: must NOT be authenticated (redirect logged-in users) ──
function RequireGuest({ children }) {
    const { isAuthenticated, isLoading, user } = useAuth();
    if (isLoading) return null;
    if (!isAuthenticated) return children;
    // Redirect based on role
    const home = { DOCTOR:'/doctor/patients', PATIENT:'/patient/dashboard', ADMIN:'/admin/dashboard', AITRAINER:'/trainer/dashboard' };
    return <Navigate to={home[user?.role] ?? '/'} replace />;
}

// ── Default redirect after login ──────────────────────────────
function RoleRedirect() {
    const { user } = useAuth();
    const home = { DOCTOR:'/doctor/patients', PATIENT:'/patient/dashboard', ADMIN:'/admin/dashboard', AITRAINER:'/trainer/dashboard' };
    return <Navigate to={home[user?.role] ?? '/login'} replace />;
}

export default function AppRoutes() {
    return (
        <Routes>
            {/* Public */}
            <Route path="/login"           element={<RequireGuest><Login /></RequireGuest>} />
            <Route path="/forgot-password" element={<RequireGuest><ForgotPassword /></RequireGuest>} />

            {/* Doctor */}
            <Route path="/doctor/patients"           element={<RequireAuth><PatientList /></RequireAuth>} />
            <Route path="/doctor/diagnosis"          element={<RequireAuth><Diagnosis /></RequireAuth>} />
            <Route path="/doctor/diagnosis/:sessionId" element={<RequireAuth><Diagnosis /></RequireAuth>} />

            {/* Patient */}
            <Route path="/patient/dashboard" element={<RequireAuth><PatientDashboard /></RequireAuth>} />
            <Route path="/patient/history"   element={<RequireAuth><History /></RequireAuth>} />

            {/* Admin */}
            <Route path="/admin/dashboard" element={<RequireAuth><AdminDashboard /></RequireAuth>} />

            {/* Root → role-based redirect */}
            <Route path="/" element={<RequireAuth><RoleRedirect /></RequireAuth>} />

            {/* 404 fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
