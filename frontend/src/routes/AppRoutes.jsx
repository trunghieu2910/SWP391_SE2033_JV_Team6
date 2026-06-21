import { createBrowserRouter, Navigate } from 'react-router';
import DashboardLayout from '../layouts/DashboardLayout';
import Login from '../pages/admin/Login';
import Dashboard from '../pages/admin/Dashboard';
import UserManagement from '../pages/admin/Users/UserManagement';
import UserDetail from '../pages/admin/UserDetail.jsx';
import SystemLogs from '../pages/admin/SystemLogs';
import CreateDoctor from '../pages/admin/CreateDoctor';
import { PrivateRoute } from './PrivateRoute';
import { AdminRoute } from './AdminRoute';
import DoctorLayout from '../components/doctor/layout/DoctorLayout';
import DoctorDashboard from '../pages/doctor/DoctorDashboard';
import SessionDetail from '../pages/doctor/SessionDetail';
import DoctorProfile from '../pages/doctor/DoctorProfile';

export const router = createBrowserRouter([
    {
        path: '/login',
        element: <Login />,
    },
    {
        path: '/',
        element: (
            <PrivateRoute>
                <DashboardLayout />
            </PrivateRoute>
        ),
        children: [
            {
                index: true,
                element: <Navigate to="/dashboard" replace />,
            },
            {
                path: 'dashboard',
                element: (
                    <AdminRoute>
                        <Dashboard />
                    </AdminRoute>
                ),
            },
            {
                path: 'users',
                element: (
                    <AdminRoute>
                        <UserManagement />
                    </AdminRoute>
                ),
            },
            {
                path: 'users/:userId',
                element: (
                    <AdminRoute>
                        <UserDetail />
                    </AdminRoute>
                ),
            },
            {
                path: 'logs',
                element: (
                    <AdminRoute>
                        <SystemLogs />
                    </AdminRoute>
                ),
            },
            {
                path: 'create-doctor',
                element: (
                    <AdminRoute>
                        <CreateDoctor />
                    </AdminRoute>
                ),
            },
        ],
    },
    {
        path: '/doctor',
        element: (
            <PrivateRoute>
                <DoctorLayout />
            </PrivateRoute>
        ),
        children: [
            {
                index: true,
                element: <Navigate to="/doctor/dashboard" replace />,
            },
            {
                path: 'dashboard',
                element: <DoctorDashboard />,
            },
            {
                path: 'sessions/:id',
                element: <SessionDetail />,
            },
            {
                path: 'profile',
                element: <DoctorProfile />,
            },
        ],
    },

    {
        path: '*',
        element: <Navigate to="/dashboard" replace />,
    },
]);