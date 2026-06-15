import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LayoutDashboard, Users, FileSpreadsheet, UserPlus, LogOut, User as UserIcon } from 'lucide-react';
import '../styles/admin.css';

export const AdminLayout = ({ children }) => {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  const menuItems = [
    { path: '/admin/dashboard', name: 'Dashboard', icon: <LayoutDashboard size={20} /> },
    { path: '/admin/users', name: 'Quản lý người dùng', icon: <Users size={20} /> },
    { path: '/admin/logs', name: 'Quản lý hệ thống logs', icon: <FileSpreadsheet size={20} /> },
    { path: '/admin/create-doctor', name: 'Tạo tài khoản Bác sĩ', icon: <UserPlus size={20} /> }
  ];

  return (
    <div className="admin-layout">
      {/* Left Sidebar */}
      <aside className="admin-sidebar">
        <div className="sidebar-brand">
          <h3>UT Cancer Admin</h3>
        </div>

        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-label">{item.name}</span>
            </NavLink>
          ))}
        </nav>

        {/* Bottom Profile and Logout */}
        <div className="sidebar-footer">
          <div className="admin-profile">
            <div className="profile-icon">
              <UserIcon size={18} />
            </div>
            <div className="profile-info text-truncate">
              <span className="profile-name">{user?.fullName || 'Admin User'}</span>
              <span className="profile-role">Quản trị viên</span>
            </div>
          </div>
          <button onClick={handleLogout} className="logout-btn btn btn-outline-danger w-100 mt-2 d-flex align-items-center justify-content-center gap-2">
            <LogOut size={16} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* Right Content */}
      <main className="admin-content">
        <header className="content-header shadow-sm bg-white d-flex align-items-center px-4">
          <h4 className="m-0 text-dark font-weight-bold">Hệ Thống Quản Trị Hỗ Trợ Chẩn Đoán Ung Thư Cổ Tử Cung</h4>
        </header>
        <div className="content-body bg-light p-4">
          <div className="bg-white rounded p-4 shadow-sm min-vh-100">
            {children}
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
