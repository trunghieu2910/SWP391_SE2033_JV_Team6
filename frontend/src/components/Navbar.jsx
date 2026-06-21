import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import './Navbar.css';

const ROLE_COLORS = {
  DOCTOR:    { bg: '#E0F5F5', color: '#0A9396' },
  PATIENT:   { bg: '#EBF2FF', color: '#1B4F9B' },
  ADMIN:     { bg: '#F3E8FF', color: '#7C3AED' },
  AITRAINER: { bg: '#FFF3E0', color: '#E67E22' },
};

function getInitials(name = '') {
  return name.split(' ').map(n => n[0]).slice(0,2).join('').toUpperCase() || '?';
}

export default function Navbar({ onLogoutSuccess }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dropOpen, setDropOpen]     = useState(false);
  const [showModal, setShowModal]   = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const dropRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) setDropOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await authService.logout();
    } catch { /* ignore – still log out client-side */ }
    logout();
    setLoggingOut(false);
    setShowModal(false);
    onLogoutSuccess?.();
    navigate('/login');
  };

  const role = user?.role ?? '';
  const roleStyle = ROLE_COLORS[role] ?? ROLE_COLORS.PATIENT;

  return (
    <>
      <header className="navbar">
        <div className="navbar-left">
          {/* breadcrumb or page title could go here */}
        </div>

        <div className="navbar-right" ref={dropRef}>
          <button className="navbar-user-btn" onClick={() => setDropOpen(p => !p)}>
            <div className="navbar-avatar">{getInitials(user?.fullName)}</div>
            <div className="navbar-user-info">
              <span className="navbar-fullname">{user?.fullName ?? 'User'}</span>
              <span className="navbar-role-badge" style={{ background: roleStyle.bg, color: roleStyle.color }}>
                {role}
              </span>
            </div>
            <span className="navbar-chevron">{dropOpen ? '▲' : '▼'}</span>
          </button>

          {dropOpen && (
            <div className="navbar-dropdown">
              <button className="navbar-dropdown-item" onClick={() => { navigate('/profile'); setDropOpen(false); }}>
                <span>👤</span> My Profile
              </button>
              <div className="navbar-dropdown-divider" />
              <button
                className="navbar-dropdown-item danger"
                onClick={() => { setShowModal(true); setDropOpen(false); }}
              >
                <span>🚪</span> Sign Out
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Logout Confirmation Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span style={{ fontSize: '2.5rem' }}>🚪</span>
              <h3 className="modal-title">Sign Out?</h3>
            </div>
            <p className="modal-body">
              You will be logged out of all sessions and your current token will be invalidated.
            </p>
            <div className="modal-actions">
              <button className="btn btn-outline" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button className="btn btn-danger" onClick={handleLogout} disabled={loggingOut}>
                {loggingOut ? <span className="spinner" /> : null}
                {loggingOut ? 'Signing out…' : 'Sign Out'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
