import { useContext } from 'react'
import { AuthContext } from '../../context/AuthContext.jsx'

export default function Navbar() {
    const { user } = useContext(AuthContext) ?? {}

    const fullName = user?.fullName || user?.username || 'Bác sĩ'
    const initials = fullName
        .split(' ')
        .map(w => w[0])
        .slice(-2)
        .join('')
        .toUpperCase()

    return (
        <nav className="navbar z1">
            <div className="nav-brand">
                <div className="nav-logo">🏥</div>
                <div className="nav-titles">
                    <span>MedRecord Pro</span>
                    <span>HỆ THỐNG QUẢN LÝ BỆNH ÁN</span>
                </div>
            </div>

            <div className="nav-user">
                <div className="nav-avatar">{initials || 'BS'}</div>
                <div>
                    <div className="nav-uname">{fullName}</div>
                    <div className="nav-urole">{user?.role === 'ROLE_DOCTOR' ? 'Bác sĩ điều trị' : (user?.role || 'Bác sĩ điều trị')}</div>
                </div>
                <div className="online-dot" title="Đang trực tuyến" />
            </div>
        </nav>
    )
}
