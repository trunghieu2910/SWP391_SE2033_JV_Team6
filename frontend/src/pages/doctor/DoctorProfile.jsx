import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaPhone, FaCalendar, FaVenusMars, FaMapMarkerAlt, FaIdCard, FaEdit, FaSave, FaTimes, FaHistory, FaStethoscope, FaClipboardList, FaCheckCircle, FaClock, FaUserMd } from 'react-icons/fa';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const DoctorProfile = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [profile, setProfile] = useState(null);
    const [formData, setFormData] = useState({
        fullName: '',
        phoneNumber: '',
        nationalID: '',
        gender: '',
        dob: '',
        address: ''
    });
    const [originalData, setOriginalData] = useState(null);

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        setLoading(true);
        try {
            const response = await doctorService.getProfile();
            setProfile(response.data);
            // Map dữ liệu vào form
            const data = {
                fullName: response.data.fullName || '',
                phoneNumber: response.data.phoneNumber || '',
                nationalID: response.data.nationalID || '',
                gender: response.data.gender || '',
                dob: response.data.dob || '',
                address: response.data.address || ''
            };
            setFormData(data);
            setOriginalData(data);
        } catch (error) {
            console.error('Error fetching profile:', error);
            toast.error('Không thể tải thông tin hồ sơ');
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = () => {
        setFormData(originalData);
        setIsEditing(true);
    };

    const handleCancel = () => {
        setFormData(originalData);
        setIsEditing(false);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await doctorService.updateProfile(formData);
            setProfile(response.data);
            setOriginalData(formData);
            setIsEditing(false);
            toast.success('Cập nhật hồ sơ thành công');
        } catch (error) {
            console.error('Error updating profile:', error);
            toast.error(error.response?.data?.message || 'Không thể cập nhật hồ sơ');
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (date) => {
        if (!date) return '—';
        return new Date(date).toLocaleDateString('vi-VN');
    };

    const formatDateTime = (date) => {
        if (!date) return '—';
        return new Date(date).toLocaleString('vi-VN');
    };

    const getStatusBadge = (status) => {
        const statusMap = {
            ACTIVE: { label: 'Đang hoạt động', color: 'bg-green-100 text-green-700' },
            INACTIVE: { label: 'Không hoạt động', color: 'bg-gray-100 text-gray-700' },
            BANNED: { label: 'Đã bị khóa', color: 'bg-red-100 text-red-700' },
            PENDING: { label: 'Chờ xác nhận', color: 'bg-yellow-100 text-yellow-700' }
        };
        const s = statusMap[status] || { label: status, color: 'bg-gray-100 text-gray-700' };
        return <span className={`px-3 py-1 rounded-full text-xs font-medium ${s.color}`}>{s.label}</span>;
    };

    // Thống kê giả - sẽ thay bằng API thật sau
    const stats = {
        totalSessions: 245,
        processing: 12,
        completed: 233,
        patients: 189
    };

    if (loading) return <LoadingSpinner />;
    if (!profile) return null;

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Hồ sơ của tôi</h1>

            {/* Card thông tin chính */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
                <div className="flex items-start justify-between">
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-[#100357] rounded-full flex items-center justify-center text-white text-2xl font-bold">
                            {profile.fullName?.charAt(0) || profile.username?.charAt(0) || 'B'}
                        </div>
                        <div>
                            <h2 className="text-2xl font-bold text-gray-800">{profile.fullName}</h2>
                            <div className="flex items-center gap-3 mt-1">
                                <span className="text-sm text-gray-500 flex items-center gap-1">
                                    <FaUserMd className="w-3 h-3" /> Bác sĩ
                                </span>
                                <span className="text-sm text-gray-500 flex items-center gap-1">
                                    <FaStethoscope className="w-3 h-3" /> Chuyên khoa: Ung thư phụ khoa
                                </span>
                            </div>
                            <div className="mt-2 flex items-center gap-3">
                                {getStatusBadge(profile.status)}
                                <span className="text-xs text-gray-400">
                                    Tham gia: {formatDate(profile.createdAt)}
                                </span>
                            </div>
                        </div>
                    </div>
                    {!isEditing && (
                        <button
                            onClick={handleEdit}
                            className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition flex items-center gap-2"
                        >
                            <FaEdit className="w-4 h-4" /> Chỉnh sửa hồ sơ
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Cột trái: Thông tin cá nhân */}
                <div className="lg:col-span-2">
                    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                        <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <FaUser className="text-[#100357]" /> Thông tin cá nhân
                        </h3>

                        {isEditing ? (
                            <form onSubmit={handleSubmit} className="space-y-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Họ và tên <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            name="fullName"
                                            value={formData.fullName}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Số điện thoại
                                        </label>
                                        <input
                                            type="text"
                                            name="phoneNumber"
                                            value={formData.phoneNumber}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            CCCD/CMND
                                        </label>
                                        <input
                                            type="text"
                                            name="nationalID"
                                            value={formData.nationalID}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Giới tính
                                        </label>
                                        <select
                                            name="gender"
                                            value={formData.gender}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                        >
                                            <option value="">Chọn giới tính</option>
                                            <option value="Male">Nam</option>
                                            <option value="Female">Nữ</option>
                                            <option value="Other">Khác</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Ngày sinh
                                        </label>
                                        <input
                                            type="date"
                                            name="dob"
                                            value={formData.dob}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                        />
                                    </div>
                                    <div className="md:col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Địa chỉ
                                        </label>
                                        <input
                                            type="text"
                                            name="address"
                                            value={formData.address}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                        />
                                    </div>
                                </div>
                                <div className="flex gap-3 pt-2">
                                    <button
                                        type="button"
                                        onClick={handleCancel}
                                        className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition flex items-center gap-2"
                                    >
                                        <FaTimes className="w-4 h-4" /> Hủy
                                    </button>
                                    <button
                                        type="submit"
                                        className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition flex items-center gap-2"
                                    >
                                        <FaSave className="w-4 h-4" /> Lưu thay đổi
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Họ và tên</p>
                                    <p className="text-gray-800 font-medium">{profile.fullName || '—'}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Tên đăng nhập</p>
                                    <p className="text-gray-800 font-medium">{profile.username}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Email</p>
                                    <p className="text-gray-800 font-medium">{profile.email}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Số điện thoại</p>
                                    <p className="text-gray-800 font-medium">{profile.phoneNumber || '—'}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">CCCD/CMND</p>
                                    <p className="text-gray-800 font-medium">{profile.nationalID || '—'}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Giới tính</p>
                                    <p className="text-gray-800 font-medium">
                                        {profile.gender === 'Male' ? 'Nam' : profile.gender === 'Female' ? 'Nữ' : '—'}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Ngày sinh</p>
                                    <p className="text-gray-800 font-medium">{formatDate(profile.dob)}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Địa chỉ</p>
                                    <p className="text-gray-800 font-medium">{profile.address || '—'}</p>
                                </div>
                                <div className="md:col-span-2 pt-2 border-t border-gray-100">
                                    <p className="text-xs text-gray-400 uppercase">Vai trò</p>
                                    <p className="text-gray-800 font-medium">Bác sĩ</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Cột phải: Thống kê + Bảo mật */}
                <div className="lg:col-span-1 space-y-6">
                    {/* Thống kê */}
                    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                        <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <FaClipboardList className="text-[#100357]" /> Thống kê hoạt động
                        </h3>
                        <div className="space-y-3">
                            <div className="flex justify-between items-center pb-2 border-b border-gray-100">
                                <span className="text-sm text-gray-600 flex items-center gap-2">
                                    <FaHistory className="w-4 h-4 text-gray-400" /> Tổng ca
                                </span>
                                <span className="font-bold text-gray-800">{stats.totalSessions}</span>
                            </div>
                            <div className="flex justify-between items-center pb-2 border-b border-gray-100">
                                <span className="text-sm text-gray-600 flex items-center gap-2">
                                    <FaClock className="w-4 h-4 text-yellow-500" /> Đang xử lý
                                </span>
                                <span className="font-bold text-yellow-600">{stats.processing}</span>
                            </div>
                            <div className="flex justify-between items-center pb-2 border-b border-gray-100">
                                <span className="text-sm text-gray-600 flex items-center gap-2">
                                    <FaCheckCircle className="w-4 h-4 text-green-500" /> Hoàn thành
                                </span>
                                <span className="font-bold text-green-600">{stats.completed}</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600 flex items-center gap-2">
                                    <FaUser className="w-4 h-4 text-blue-500" /> Bệnh nhân
                                </span>
                                <span className="font-bold text-blue-600">{stats.patients}</span>
                            </div>
                        </div>
                    </div>

                    {/* Bảo mật */}
                    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                        <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <FaIdCard className="text-[#100357]" /> Bảo mật
                        </h3>
                        <div className="space-y-3">
                            <div className="flex justify-between items-center pb-2 border-b border-gray-100">
                                <span className="text-sm text-gray-600">Trạng thái</span>
                                {getStatusBadge(profile.status)}
                            </div>
                            <div className="flex justify-between items-center pb-2 border-b border-gray-100">
                                <span className="text-sm text-gray-600">Lần cuối đăng nhập</span>
                                <span className="text-sm text-gray-800">{formatDateTime(profile.lastLoginTime) || '—'}</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">Đổi mật khẩu</span>
                                <button
                                    onClick={() => navigate('/change-password')}
                                    className="text-sm text-[#100357] hover:underline font-medium"
                                >
                                    Thay đổi
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DoctorProfile;