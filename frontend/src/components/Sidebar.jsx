import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Sidebar.css';

const MENUS = {
  DOCTOR:    [
    { path: '/doctor/patients',  label: 'Patient List',    icon: '👥' },
    { path: '/doctor/diagnosis', label: 'Diagnosis',       icon: '🔬' },
  ],
  PATIENT:   [
    { path: '/patient/dashboard',label: 'Dashboard',       icon: '🏠' },
    { path: '/patient/history',  label: 'My History',      icon: '📋' },
    { path: '/patient/upload',   label: 'Upload Image',    icon: '🖼️' },
  ],
  ADMIN:     [
    { path: '/admin/dashboard',  label: 'Dashboard',       icon: '📊' },
    { path: '/admin/users',      label: 'User Management', icon: '👤' },
  ],
  AITRAINER: [
    { path: '/trainer/dashboard',label: 'Dashboard',       icon: '🤖' },
  ],
};

export default function Sidebar() {
  const { user } = useAuth();
  const role = user?.role ?? 'PATIENT';
  const items = MENUS[role] ?? [];

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span>+</span>
        </div>
        <div>
          <div className="sidebar-app-name">MedAI</div>
          <div className="sidebar-tagline">Diagnosis System</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {items.map(item => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `sidebar-item${isActive ? ' active' : ''}`
            }
          >
            <span className="sidebar-icon">{item.icon}</span>
            <span className="sidebar-label">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <NavLink
          to="/profile"
          className={({ isActive }) =>
            `sidebar-item${isActive ? ' active' : ''}`
          }
        >
          <span className="sidebar-icon">⚙️</span>
          <span className="sidebar-label">Profile</span>
        </NavLink>
      </div>
    </aside>
  );
}
