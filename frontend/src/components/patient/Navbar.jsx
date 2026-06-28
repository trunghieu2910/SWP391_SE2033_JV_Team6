import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Activity, FileText, LogOut, User } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showConfirm, setShowConfirm] = useState(false);

  const handleLogoutClick = () => setShowConfirm(true);

  const handleLogoutConfirm = () => {
    setShowConfirm(false);
    logout();
    navigate('/login');
  };

  const username = user?.username || 'patient_nam';
  const fullName = user?.fullName || 'Bệnh nhân';

  return (
    <nav className="w-full bg-white rounded-2xl shadow-sm border border-gray-100 px-6 py-3 flex items-center justify-between mb-6">
      {/* Left section: Logo & Nav items */}
      <div className="flex items-center gap-8">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600">
            <Activity className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <div className="font-bold text-xl text-blue-950 tracking-tight leading-none">MedAI</div>
            <div className="text-[10px] text-gray-500 mt-0.5 font-medium max-w-[280px] leading-tight">
              Hệ thống sàng lọc ung thư cổ tử cung hỗ trợ bởi trí tuệ nhân tạo
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="hidden md:flex items-center gap-3">
          <NavLink
            to="/patient/home"
            className={({ isActive }) =>
              `flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-sm shadow-blue-200'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-blue-900'
              }`
            }
          >
            <Activity className="w-4 h-4" />
            <span>Trang chính</span>
          </NavLink>
          <NavLink
            to="/patient/medical-records"
            className={({ isActive }) =>
              `flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-sm shadow-blue-200'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-blue-900'
              }`
            }
          >
            <FileText className="w-4 h-4" />
            <span>Hồ sơ bệnh án</span>
          </NavLink>
          <NavLink
            to="/patient/profile"
            className={({ isActive }) =>
              `flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-sm shadow-blue-200'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-blue-900'
              }`
            }
          >
            <User className="w-4 h-4" />
            <span>Hồ sơ cá nhân</span>
          </NavLink>
        </div>
      </div>

      {/* Right section: User profile & Logout */}
      <div className="flex items-center gap-4">
        {/* User profile info */}
        <div 
          className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition-opacity duration-150"
          onClick={() => navigate('/patient/profile')}
        >
          <div className="text-right">
            <div className="text-sm font-bold text-gray-800 leading-none">{username}</div>
            <div className="text-xs text-gray-400 mt-1 font-medium">Bệnh nhân</div>
          </div>
          <div className="w-10 h-10 bg-blue-50 border border-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold">
            {fullName.charAt(0).toUpperCase()}
          </div>
        </div>

        {/* Logout button */}
        <button
          onClick={handleLogoutClick}
          className="w-10 h-10 border border-gray-100 rounded-full flex items-center justify-center text-red-500 hover:bg-red-50 hover:border-red-100 transition-all duration-200 animate-none"
          title="Đăng xuất"
        >
          <LogOut className="w-5 h-5" />
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
                    className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                >
                  Đăng xuất
                </button>
              </div>
            </div>
          </div>
      )}
    </nav>
  );
}
