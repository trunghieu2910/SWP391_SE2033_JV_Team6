import { useState, useEffect } from 'react';
import axios from 'axios';

const useMedicalRecords = () => {
    // 1. Quản lý trạng thái dữ liệu
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(true);

    // 2. Lấy thông tin user đăng nhập
    const currentUser = JSON.parse(localStorage.getItem('user')) || {
        role: 'DOCTOR',
        id: 1,
        name: 'Nguyễn Văn Tùng'
    };

    // 3. Logic gọi API
    useEffect(() => {
        const fetchMedicalRecords = async () => {
            try {
                const apiUrl = currentUser.role === 'DOCTOR'
                    ? 'http://localhost:8080/api/medical-records'
                    : `http://localhost:8080/api/medical-records/patient/${currentUser.id}`;

                const response = await axios.get(apiUrl);
                setRecords(Array.isArray(response.data) ? response.data : [response.data]);
                setLoading(false);
            } catch (error) {
                console.error("Lỗi gọi API bệnh án:", error);
                setLoading(false);
            }
        };

        fetchMedicalRecords();
    }, [currentUser.id, currentUser.role]);

    // 4. Trả về đúng những gì giao diện cần
    return { records, loading, currentUser };
};

export default useMedicalRecords;