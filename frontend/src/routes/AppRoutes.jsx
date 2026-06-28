import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// Auth pages
import Login          from '../pages/auth/Login';
import ForgotPassword from '../pages/auth/ForgotPassword';
import Register       from '../pages/auth/Register';

// Doctor pages
import PatientList      from '../pages/doctor/PatientList';
import Diagnosis        from '../pages/doctor/Diagnosis';
import DoctorDashboard  from '../pages/doctor/DoctorDashboard';
import SessionDetail    from '../pages/doctor/SessionDetail';
import DoctorProfile    from '../pages/doctor/DoctorProfile';
import AiAssistant      from '../pages/doctor/AiAssistant';
import DoctorLayout     from '../components/doctor/layout/DoctorLayout';

// Patient pages
import PatientDashboard from '../pages/patient/Dashboard';
import History          from '../pages/patient/History';
import PatientLayout    from '../layouts/PatientLayout';
import MedicalRecordDetail from '../pages/patient/MedicalRecordDetail';
import Profile          from '../pages/Profile';

// Admin pages
import AdminDashboard from '../pages/admin/Dashboard';
import UserManagement from '../pages/admin/UserManagement';
import UserDetail     from '../pages/admin/UserDetail';
import SystemLogs     from '../pages/admin/SystemLogs';
import CreateDoctor   from '../pages/admin/CreateDoctor';
import AdminLayout    from '../components/admin/layout/AdminLayout';

// Medical Records pages
import MedicalRecordsPage from '../pages/medical-records/MedicalRecordsPage';
import MedicalRecordDetailPage from '../pages/medical-records/MedicalRecordDetailPage';

const HOME_BY_ROLE = {
    DOCTOR:    '/doctor/dashboard',
    PATIENT:   '/patient/home',
    ADMIN:     '/admin/dashboard',
    AITRAINER: '/trainer/dashboard',
};

function RequireAuth({ children, allowedRoles }) {
    const { isAuthenticated, isLoading, user } = useAuth();
    if (isLoading) {
        return (
            <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:'100vh' }}>
                <span className="spinner spinner-dark" />
            </div>
        );
    }
    if (!isAuthenticated) return <Navigate to="/login" replace />;
    if (allowedRoles && !allowedRoles.includes(user?.role)) {
        return <Navigate to={HOME_BY_ROLE[user?.role] ?? '/'} replace />;
    }
    return children;
}

function RequireGuest({ children }) {
    const { isAuthenticated, isLoading, user } = useAuth();
    if (isLoading) return null;
    if (!isAuthenticated) return children;
    return <Navigate to={HOME_BY_ROLE[user?.role] ?? '/'} replace />;
}

function RoleRedirect() {
    const { user } = useAuth();
    return <Navigate to={HOME_BY_ROLE[user?.role] ?? '/login'} replace />;
}

export default function AppRoutes() {
    return (
        <Routes>
            {/* Public */}
            <Route path="/login"           element={<RequireGuest><Login /></RequireGuest>} />
            <Route path="/register"        element={<RequireGuest><Register /></RequireGuest>} />
            <Route path="/forgot-password" element={<RequireGuest><ForgotPassword /></RequireGuest>} />

            {/* Doctor */}
            <Route
                path="/doctor"
                element={<RequireAuth allowedRoles={[ 'DOCTOR' ]}><DoctorLayout /></RequireAuth>}
            >
                <Route path="dashboard" element={<DoctorDashboard />} />
                <Route path="patients" element={<PatientList />} />
                <Route path="create-session" element={<PatientList />} />
                <Route path="diagnosis" element={<Diagnosis />} />
                <Route path="diagnosis/:sessionId" element={<Diagnosis />} />
                <Route path="sessions/:sessionId" element={<SessionDetail />} />
                <Route path="medical-records" element={<MedicalRecordsPage />} />
                <Route path="medical-records/:id" element={<MedicalRecordDetailPage />} />
                <Route path="profile" element={<DoctorProfile />} />
                <Route path="ai-assistant" element={<AiAssistant />} />
            </Route>

            {/* Patient */}
            <Route
                path="/patient"
                element={<RequireAuth allowedRoles={[ 'PATIENT' ]}><PatientLayout /></RequireAuth>}
            >
                <Route path="home" element={<PatientDashboard />} />
                <Route path="profile" element={<Profile />} />
                <Route path="medical-records" element={<History />} />
                <Route path="medical-record/:id" element={<MedicalRecordDetail />} />
                <Route index element={<Navigate to="home" replace />} />
            </Route>

            {/* Admin */}
            <Route
                path="/admin"
                element={<RequireAuth allowedRoles={[ 'ADMIN' ]}><AdminLayout /></RequireAuth>}
            >
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="users" element={<UserManagement />} />
                <Route path="users/:userId" element={<UserDetail />} />
                <Route path="logs" element={<SystemLogs />} />
                <Route path="create-doctor" element={<CreateDoctor />} />
                <Route index element={<Navigate to="dashboard" replace />} />
            </Route>

            {/* Medical Records */}
            <Route
                path="/medical-records"
                element={<RequireAuth allowedRoles={[ 'DOCTOR', 'PATIENT', 'ADMIN' ]}><MedicalRecordsPage /></RequireAuth>}
            />
            <Route
                path="/medical-records/:id"
                element={<RequireAuth allowedRoles={[ 'DOCTOR', 'PATIENT', 'ADMIN' ]}><MedicalRecordDetailPage /></RequireAuth>}
            />

            {/* Root → role-based redirect */}
            <Route path="/" element={<RequireAuth><RoleRedirect /></RequireAuth>} />

            {/* 404 fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
