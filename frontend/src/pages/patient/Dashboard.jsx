import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import api from '../../services/api';
import PatientProfile from '../../components/patient/PatientProfile';
import ActiveSession from '../../components/patient/ActiveSession';
import MedicalHistory from '../../components/patient/MedicalHistory';

export default function PatientDashboard() {
  const { user } = useContext(AuthContext);
  const [profile, setProfile] = useState(null);
  const [activeSession, setActiveSession] = useState(null);
  const [recentRecords, setRecentRecords] = useState([]);
  const [showForm, setShowForm] = useState(false);
  
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [formSubmitLoading, setFormSubmitLoading] = useState(false);
  const [message, setMessage] = useState(null);

  // 1. Fetch patient profile
  const fetchProfile = async () => {
    try {
      setLoadingProfile(true);
      const res = await api.get('/api/profile');
      setProfile(res.data);
      return res.data;
    } catch (err) {
      console.error('Error fetching profile', err);
      return null;
    } finally {
      setLoadingProfile(false);
    }
  };

  // 2. Fetch patient active session & completed medical records
  const fetchSessionsData = async (profileData) => {
    if (!profileData) return;
    try {
      setLoadingSessions(true);
      
      // Fetch Active Session from backend
      const activeRes = await api.get('/api/patient/sessions/active');
      let active = activeRes.data?.data || null;

      if (active) {
        // Fetch symptom result details if active session exists
        try {
          const symptomRes = await api.get(`/api/diagnosis-sessions/${active.sessionId}/symptom-result`);
          if (symptomRes.data && symptomRes.data.data) {
            active.symptomResult = symptomRes.data.data;
          }
        } catch (e) {
          // No symptom result submitted yet or not found
        }
      }
      setActiveSession(active);

      // Fetch Medical Records using patient's nationalID or fullName as keyword
      const keyword = profileData.nationalID || profileData.fullName || undefined;
      if (keyword) {
        const recordsRes = await api.get('/api/medical-records', {
          params: { keyword, page: 0, size: 20 }
        });
        
        const list = recordsRes.data?.content || [];
        
        // Group by record.id to eliminate duplicate record IDs
        const recordMap = {};
        for (const record of list) {
          if (!recordMap[record.id]) {
            recordMap[record.id] = {
              ...record,
              symptomsList: record.symptoms ? [record.symptoms] : []
            };
          } else {
            if (record.symptoms && !recordMap[record.id].symptomsList.includes(record.symptoms)) {
              recordMap[record.id].symptomsList.push(record.symptoms);
            }
          }
        }
        
        const uniqueRecords = Object.values(recordMap).map(record => ({
          ...record,
          symptoms: record.symptomsList.join(', ') || 'Không ghi nhận'
        }));

        // Securely filter on patient's own records only
        const filtered = uniqueRecords.filter(record => 
          record.nationalID === profileData.nationalID || 
          record.patientName.trim().toLowerCase() === profileData.fullName.trim().toLowerCase()
        );

        // Completed records are those that have status COMPLETED
        const completed = filtered.filter(r => r.status === 'COMPLETED');
        setRecentRecords(completed);
      }
    } catch (err) {
      console.error('Error fetching sessions/records', err);
    } finally {
      setLoadingSessions(false);
    }
  };

  const loadAllData = async () => {
    const prof = await fetchProfile();
    if (prof) {
      await fetchSessionsData(prof);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  // Submit clinical form
  const handleFormSubmit = async (formData) => {
    if (!activeSession) return;
    
    // Transform layout form output matching backend expectation
    const intentData = {
      height: formData.height,
      weight: formData.weight,
      menopauseStatus: formData.menopauseStatus,
      symptomDuration: formData.symptomDuration,
      symptomProgressing: formData.symptomProgressing,
      symptoms: formData.symptoms || []
    };

    if (!intentData.height || !intentData.weight) {
      setMessage({ type: 'error', text: 'Bạn phải điền đầy đủ chiều cao cân nặng' });
      return;
    }

    if (parseFloat(intentData.height) <= 0 || parseFloat(intentData.weight) <= 0) {
      setMessage({ type: 'error', text: 'Chiều cao cân nặng phải là số dương' });
      return;
    }

    const isConfirmed = window.confirm("Khi bạn ấn gửi đi sẽ không được chỉnh sửa, bạn có chắc chắn muốn gửi không?");
    if (!isConfirmed) return;
    
    setFormSubmitLoading(true);
    setMessage(null);
    try {
      await api.post(`/api/diagnosis-sessions/${activeSession.sessionId}/symptom-result`, intentData);
      setMessage({ type: 'success', text: 'Gửi thông tin triệu chứng thành công! Trạng thái phiên khám đã được cập nhật.' });
      setShowForm(false);
      // Reload sessions and active session
      if (profile) {
        await fetchSessionsData(profile);
      }
    } catch (err) {
      console.error('Error submitting symptoms', err);
      setMessage({ type: 'error', text: err.response?.data?.message || 'Không thể gửi biểu mẫu. Vui lòng thử lại.' });
    } finally {
      setFormSubmitLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start text-left">
      {/* Cột trái: Thông tin bệnh nhân */}
      <div className="lg:col-span-1">
        {loadingProfile ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[300px]">
            <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            <span className="text-sm text-gray-500 mt-4">Đang tải hồ sơ...</span>
          </div>
        ) : (
          <PatientProfile profile={profile} />
        )}
      </div>

      {/* Cột phải: Phiên khám đang hoạt động & Bệnh án gần đây */}
      <div className="lg:col-span-2 space-y-6">
        {message && (
          <div className={`p-4 rounded-xl text-sm font-semibold flex items-center gap-2 border animate-fade-in ${
            message.type === 'success'
              ? 'bg-green-50 border-green-100 text-green-800'
              : 'bg-red-50 border-red-100 text-red-800'
          }`}>
            <span>{message.type === 'success' ? '✓' : '⚠️'}</span>
            <span>{message.text}</span>
          </div>
        )}

        {/* Card 1: Phiên khám đang hoạt động */}
        {loadingSessions ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[200px]">
            <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            <span className="text-sm text-gray-500 mt-4">Đang tải phiên khám...</span>
          </div>
        ) : (
          <ActiveSession
            activeSession={activeSession}
            showForm={showForm}
            setShowForm={setShowForm}
            formSubmitLoading={formSubmitLoading}
            handleFormSubmit={handleFormSubmit}
          />
        )}

        {/* Card 2: Bệnh án gần đây */}
        {loadingSessions ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[200px]">
            <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            <span className="text-sm text-gray-500 mt-4">Đang tải bệnh án...</span>
          </div>
        ) : (
          <MedicalHistory recentRecords={recentRecords} />
        )}
      </div>
    </div>
  );
}
