import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
    FaStethoscope,
    FaClipboardList,
    FaUserMd,
    FaSignOutAlt,
    FaFileMedical,
    FaRobot
} from 'react-icons/fa';
import { useAuth } from '../../../hooks/useAuth';

const menuItems = [
    { path: '/doctor/dashboard', label: 'Quản lý ca chẩn đoán', icon: FaClipboardList },
    { path: '/doctor/create-session', label: 'Tạo ca chẩn đoán', icon: FaStethoscope },
    { path: '/doctor/medical-records', label: 'Xem hồ sơ bệnh án', icon: FaFileMedical },
    { path: '/doctor/profile', label: 'Hồ sơ bác sĩ', icon: FaUserMd },
    { path: '/doctor/ai-assistant', label: 'Trợ lý AI', icon: FaRobot },
];

const DoctorSidebar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [showConfirm, setShowConfirm] = useState(false);

    // Chỉ mở modal xác nhận, chưa logout
    const handleLogoutClick = () => setShowConfirm(true);

    // Thực sự logout khi user bấm Xác nhận
    const handleLogoutConfirm = async () => {
        setShowConfirm(false);
        await logout();
        navigate('/login');
    };

    // Lấy chữ cái đầu từ tên bác sĩ
    const getAvatarInitial = () => {
        const fullName = user?.fullName || user?.username || 'Bác sĩ';
        // Lấy chữ cái đầu tiên
        const firstChar = fullName.charAt(0).toUpperCase();
        return firstChar;
    };

    return (
        <div className="fixed left-4 top-4 bottom-4 w-64 bg-white rounded-2xl shadow-lg flex flex-col z-50">
            {/* Logo */}
            <div className="p-6 border-b border-gray-100">
                <h1 className="text-[#100357] text-xl font-bold">Cervical Dx</h1>
                <p className="text-gray-400 text-xs mt-1">Doctor Portal</p>
            </div>

            {/* Navigation Menu */}
            <nav className="flex-1 py-6 px-3 flex flex-col gap-y-2">
                {menuItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) =>
                            `flex items-center gap-3 px-4 py-2.5 rounded-xl transition ${
                                isActive
                                    ? 'bg-[#100357] text-white'
                                    : 'text-gray-600 hover:bg-gray-100 hover:text-[#100357]'
                            }`
                        }
                    >
                        <item.icon className="w-5 h-5" />
                        <span>{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            {/* User Profile - Ở DƯỚI CÙNG */}
            <div className="p-4 border-t border-gray-100">
                <div className="flex items-center gap-3 mb-3">
                    {/* Avatar với chữ cái đầu */}
                    <div className="w-10 h-10 bg-[#100357] rounded-full flex items-center justify-center text-white font-bold text-lg">
                        {getAvatarInitial()}
                    </div>
                    <div className="flex-1">
                        <p className="text-gray-800 text-sm font-medium">
                            {user?.fullName || user?.username || 'Bác sĩ'}
                        </p>
                        <p className="text-gray-400 text-xs">Bác sĩ</p>
                    </div>
                </div>
                <button
                    onClick={handleLogoutClick}
                    className="flex items-center gap-2 text-gray-500 hover:text-[#100357] transition w-full px-4 py-2.5 rounded-xl hover:bg-gray-100"
                >
                    <FaSignOutAlt className="w-5 h-5" />
                    <span>Đăng xuất</span>
                </button>
            </div>

            {showConfirm && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100]">
                    <div className="bg-white rounded-xl p-6 w-80 shadow-xl">
                        <h3 className="text-gray-800 font-semibold text-lg mb-2">Xác nhận đăng xuất</h3>
                        <p className="text-gray-500 text-sm mb-6">Bạn có chắc chắn muốn đăng xuất không?</p>
                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowConfirm(false)}
                                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition text-gray-600"
                            >
                                Hủy
                            </button>
                            <button
                                onClick={handleLogoutConfirm}
                                className="flex-1 px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                            >
                                Đăng xuất
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DoctorSidebar;