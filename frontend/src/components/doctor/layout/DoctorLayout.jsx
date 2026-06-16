import React from 'react';
import { Outlet } from 'react-router-dom';
import DoctorSidebar from './DoctorSidebar';

const DoctorLayout = () => {
    return (
        <div className="min-h-screen bg-gray-100">
            <DoctorSidebar />
            {/* Content với margin trái + padding để tạo gap */}
            <div className="ml-80 p-6">
                <div className="bg-white rounded-2xl shadow-sm min-h-[calc(100vh-3rem)]">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default DoctorLayout;