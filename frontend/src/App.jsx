import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import AdminLayout from './components/admin/layout/AdminLayout';
import DoctorLayout from './components/doctor/layout/DoctorLayout';
import Dashboard from './pages/admin/Dashboard';
import UserManagement from './pages/admin/UserManagement';
import UserDetail from './pages/admin/UserDetail';
import SystemLogs from './pages/admin/SystemLogs';
import CreateDoctor from './pages/admin/CreateDoctor';
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import SessionDetail from './pages/doctor/SessionDetail';
import { useAuth } from './hooks/useAuth';

const PrivateRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useAuth();
    if (loading) return null;
    if (!user) return <Navigate to="/login" />;
    if (allowedRoles && !allowedRoles.includes(user.role)) return <Navigate to="/login" />;
    return children;
};

function App() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Navigate to="/admin/dashboard" />} />

            {/* Admin Routes */}
            <Route path="/admin" element={
                <PrivateRoute allowedRoles={['ADMIN']}>
                    <AdminLayout />
                </PrivateRoute>
            }>
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="users" element={<UserManagement />} />
                <Route path="users/:id" element={<UserDetail />} />
                <Route path="logs" element={<SystemLogs />} />
                <Route path="create-doctor" element={<CreateDoctor />} />
            </Route>

            {/* Doctor Routes */}
            <Route path="/doctor" element={
                <PrivateRoute allowedRoles={['DOCTOR']}>
                    <DoctorLayout />
                </PrivateRoute>
            }>
                <Route path="dashboard" element={<DoctorDashboard />} />
                <Route path="sessions/:id" element={<SessionDetail />} />
            </Route>
        </Routes>
    );
}

export default App;