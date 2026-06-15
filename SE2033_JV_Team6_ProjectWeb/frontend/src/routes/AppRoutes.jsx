import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from '../components/common/Navbar.jsx';
import MedicalRecordsPage from '../pages/doctor/MedicalRecordsPage';
import MedicalRecordDetailPage from '../pages/doctor/MedicalRecordDetailPage';

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <div className="app-shell">
                <Navbar />
                <Routes>
                    <Route path="/" element={<Navigate to="/medical-records" replace />} />
                    <Route path="/medical-records" element={<MedicalRecordsPage />} />
                    <Route path="/medical-records/:sessionId" element={<MedicalRecordDetailPage />} />
                </Routes>
            </div>
        </BrowserRouter>
    );
}
