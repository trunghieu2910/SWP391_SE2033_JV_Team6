import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

const AdminLayout = () => {
    return (
        <div className="min-h-screen bg-gray-100">
            <Sidebar />
            {/* Khoảng cách với nền xám bên ngoài */}
            <div className="ml-64 p-4">
                {/* Nội dung bên phải có nền trắng, bo tròn */}
                <div className="bg-white rounded-xl shadow-sm min-h-[calc(100vh-2rem)]">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default AdminLayout;