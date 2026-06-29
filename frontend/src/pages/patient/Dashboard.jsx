import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import api from '../../services/api';
import PatientProfile from '../../components/patient/PatientProfile';
import ActiveSession from '../../components/patient/ActiveSession';
import MedicalHistory from '../../components/patient/MedicalHistory';
import ClinicalForm from '../../components/ClinicalForm'; // Import ClinicalForm

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

  // Submit clinical form - được gọi từ ClinicalForm
  const handleFormSubmit = async (formData) => {
    if (!activeSession) {
      setMessage({ type: 'error', text: 'Không tìm thấy phiên khám hoạt động' });
      return;
    }
    
    // Transform layout form output matching backend expectation
    // Lấy danh sách symptom IDs từ các trường trong formData
    const allSymptoms = [
      ...(formData.abnormalBleedingIds || []),
      ...(formData.abnormalDischargeIds || []),
      ...(formData.painIds || []),
      ...(formData.urinarySymptomsIds || []),
      ...(formData.digestiveSymptomsIds || [])
    ];

    // Thêm symptom IDs từ systemicSymptoms
    if (formData.systemicSymptoms) {
      if (formData.systemicSymptoms.weightLoss) allSymptoms.push(14);
      if (formData.systemicSymptoms.fatigue) allSymptoms.push(15);
      if (formData.systemicSymptoms.anorexia) allSymptoms.push(16);
    }

    // Thêm symptom IDs từ riskFactors
    if (formData.riskFactors) {
      if (formData.riskFactors.familyHistory) allSymptoms.push(25);
      if (formData.riskFactors.obesity) allSymptoms.push(26);
      if (formData.riskFactors.diabetes) allSymptoms.push(27);
      if (formData.riskFactors.hypertension) allSymptoms.push(28);
      if (formData.riskFactors.pcos) allSymptoms.push(29);
      if (formData.riskFactors.estrogenTherapy) allSymptoms.push(30);
    }

    const intentData = {
      height: formData.height,
      weight: formData.weight,
      menopauseStatus: formData.menopauseStatus,
      symptomDuration: formData.symptomDuration,
      symptomProgressing: formData.symptomProgressing,
      symptoms: allSymptoms.filter(id => id !== undefined) // Lọc bỏ undefined
    };

    // Validation
    if (!intentData.height || !intentData.weight) {
      setMessage({ type: 'error', text: 'Bạn phải điền đầy đủ chiều cao và cân nặng' });
      return;
    }

    if (parseFloat(intentData.height) <= 0 || parseFloat(intentData.weight) <= 0) {
      setMessage({ type: 'error', text: 'Chiều cao và cân nặng phải là số dương' });
      return;
    }

    if (!intentData.menopauseStatus) {
      setMessage({ type: 'error', text: 'Bạn phải chọn tình trạng mãn kinh' });
      return;
    }

    if (!intentData.symptomDuration) {
      setMessage({ type: 'error', text: 'Bạn phải chọn thời gian triệu chứng' });
      return;
    }

    if (intentData.symptomProgressing === null || intentData.symptomProgressing === undefined) {
      setMessage({ type: 'error', text: 'Bạn phải chọn diễn biến triệu chứng' });
      return;
    }

    if (intentData.symptoms.length === 0) {
      setMessage({ type: 'error', text: 'Bạn phải chọn ít nhất một triệu chứng' });
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

  // Render active session with ClinicalForm
  const renderActiveSession = () => {
    if (loadingSessions) {
      return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[200px]">
          <div className="w-8 h-8 border-4 border-[#100357] border-t-transparent rounded-full animate-spin"></div>
          <span className="text-sm text-gray-500 mt-4">Đang tải phiên khám...</span>
        </div>
      );
    }

    if (!activeSession) {
      return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="text-center py-8">
            <div className="text-4xl mb-3">📋</div>
            <h3 className="text-lg font-semibold text-gray-700">Không có phiên khám nào đang hoạt động</h3>
            <p className="text-gray-500 text-sm mt-1">Vui lòng đặt lịch khám để bắt đầu</p>
          </div>
        </div>
      );
    }

    // Nếu đã có symptomResult, hiển thị thông tin đã gửi
    if (activeSession.symptomResult?.status === 'COMPLETED') {
      return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                Phiên khám đang hoạt động
              </h3>
              <p className="text-sm text-gray-500">Mã: {activeSession.sessionId}</p>
            </div>
            <div className="flex items-center gap-3">
              <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">
                Đã gửi triệu chứng
              </span>
              <button
                onClick={() => setShowForm(!showForm)}
                className="px-4 py-2 border border-blue-600 text-blue-600 hover:bg-blue-50 text-xs font-bold rounded-xl transition-all duration-200"
              >
                {showForm ? 'Đóng thông tin' : 'Xem lại thông tin đã gửi'}
              </button>
            </div>
          </div>
          
          <div className="border-t pt-4">
            <p className="text-sm text-gray-600">
              <span className="font-medium">Trạng thái:</span> Đang chờ bác sĩ xử lý
            </p>
            <p className="text-sm text-gray-600 mt-1">
              <span className="font-medium">Ngày tạo:</span> {new Date(activeSession.createdAt).toLocaleString('vi-VN')}
            </p>
          </div>

          {showForm && (
            <div className="mt-6 border border-gray-100 rounded-2xl p-4 bg-gray-50">
              <ClinicalForm
                initialData={activeSession.symptomResult}
                readOnly={true}
              />
            </div>
          )}
        </div>
      );
    }

    // Chưa gửi triệu chứng - hiển thị form
    return (
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100">
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <span className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></span>
                Phiên khám đang hoạt động
              </h3>
              <p className="text-sm text-gray-500">Mã: {activeSession.sessionId}</p>
            </div>
            <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-sm font-medium">
              Chờ điền triệu chứng
            </span>
          </div>
        </div>

        {/* Sử dụng ClinicalForm thay vì gọi trực tiếp */}
        <div className="p-6">
          <ClinicalForm
            sessionId={activeSession.sessionId}
            initialData={activeSession.symptomResult || {}}
            onSubmit={handleFormSubmit}
            loading={formSubmitLoading}
            readOnly={activeSession.symptomResult?.status === 'COMPLETED'}
          />
        </div>
      </div>
    );
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start text-left">
      {/* Cột trái: Thông tin bệnh nhân */}
      <div className="lg:col-span-1">
        {loadingProfile ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[300px]">
            <div className="w-8 h-8 border-4 border-[#100357] border-t-transparent rounded-full animate-spin"></div>
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
        {renderActiveSession()}

        {/* Card 2: Bệnh án gần đây */}
        {loadingSessions ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center justify-center min-h-[200px]">
            <div className="w-8 h-8 border-4 border-[#100357] border-t-transparent rounded-full animate-spin"></div>
            <span className="text-sm text-gray-500 mt-4">Đang tải bệnh án...</span>
          </div>
        ) : (
          <MedicalHistory recentRecords={recentRecords} />
        )}
      </div>
    </div>
  );
}