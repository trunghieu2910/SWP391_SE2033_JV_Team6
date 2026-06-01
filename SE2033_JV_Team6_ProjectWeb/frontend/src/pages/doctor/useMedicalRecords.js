import { useState, useEffect } from 'react';
import api from '../../services/api';

const useMedicalRecords = () => {
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [refreshKey, setRefreshKey] = useState(0);


    // TODO: Khi merge, thay bằng: JSON.parse(localStorage.getItem('user'))
    const currentUser = JSON.parse(localStorage.getItem('user')) || {
        role: 'PATIENT',
        id: 2,
        name: 'Hiếu'
    };

    const { id: userId, role: userRole } = currentUser;

    useEffect(() => {
        let isMounted = true;

        const fetchMedicalRecords = async () => {
            setLoading(true);
            try {
                const endpoint = userRole === 'DOCTOR'
                    ? '/api/medical-records'
                    : `/api/medical-records/patient/${userId}`;

                const response = await api.get(endpoint);

                if (!isMounted) return;

                const data = Array.isArray(response.data) ? response.data : [response.data];
                setRecords(data);
                setError(null);

            } catch (err) {
                if (!isMounted) return;
                console.error("Lỗi gọi API bệnh án:", err);
                setError(err.response?.data?.message || err.message || "Không thể tải dữ liệu bệnh án");
                setRecords([]);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchMedicalRecords();

        return () => { isMounted = false; };
    }, [userId, userRole, refreshKey]);

    const toggleVisibility = async (sessionId, isShared) => {
        try {
            await api.put(`/api/medical-records/${sessionId}/visibility?isShared=${isShared}&doctorId=${userId}`);
            alert(isShared ? "Đã cấp quyền cho bệnh nhân xem! " : "Đã khóa bệnh án lại! ");
            setRefreshKey(prev => prev + 1);
        } catch (err) {
            console.error("Lỗi cập nhật quyền:", err);
            alert(err.response?.data?.message || "Lỗi: Không thể cập nhật quyền!");
        }
    };

    return { records, loading, error, currentUser, toggleVisibility };
};

export default useMedicalRecords;