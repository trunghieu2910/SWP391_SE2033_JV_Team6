import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/auth/Login';
import PrivateRoute from './PrivateRoute';
import AdminLayout from '../layouts/AdminLayout';
import AdminRoutes from './AdminRoutes';

export const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Login Route */}
        <Route path="/login" element={<Login />} />

        {/* Private Admin Panel Routes */}
        <Route
          path="/admin/*"
          element={
            <PrivateRoute allowedRole="ADMIN">
              <AdminLayout>
                <AdminRoutes />
              </AdminLayout>
            </PrivateRoute>
          }
        />

        {/* Default Redirect Paths */}
        <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
