import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft, FaUserCircle, FaGraduationCap, FaImage, FaFilePdf, FaFile, FaTimes } from 'react-icons/fa';
import adminService from '../../services/adminService';
import api from '../../services/api';
import Topbar from '../../components/admin/layout/Topbar';
import StatusBadge from '../../components/common/StatusBadge';
import UpdateStatusModal from '../../components/admin/users/UpdateStatusModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const UserDetail = () => {
    const { userId } = useParams();  // ✅ SỬA: userId thay vì id
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showCertificateModal, setShowCertificateModal] = useState(false);
    const [imageData, setImageData] = useState(null);
    const [imageLoading, setImageLoading] = useState(false);

    const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

    useEffect(() => {
        if (userId) {
            fetchUserDetail();
        } else {
            navigate('/admin/users');
        }
    }, [userId]);

    const fetchUserDetail = async () => {
        setLoading(true);
        try {
            const response = await adminService.getUserDetail(userId);
            console.log('📦 Full response:', response.data);
            console.log('🔍 Certificate URL:', response.data.userResponse?.certificateUrl);
            setUser(response.data.userResponse);
            setLogs(response.data.systemLogResponses || []);

            const certUrl = response.data.userResponse?.certificateUrl;
            if (certUrl) {
                fetchImageWithToken(certUrl);
            }
        } catch (error) {
            console.error('Error fetching user detail:', error);
            toast.error('Không thể tải thông tin người dùng');
            navigate('/admin/users');
        } finally {
            setLoading(false);
        }
    };

    const fetchImageWithToken = async (url) => {
        setImageLoading(true);
        try {
            let fullUrl = url;
            if (!url.startsWith('http://') && !url.startsWith('https://')) {
                if (url.startsWith('/uploads/')) {
                    fullUrl = `${API_BASE_URL}${url}`;
                } else {
                    fullUrl = `${API_BASE_URL}/uploads/certificates/${url}`;
                }
            }

            console.log('🔗 Fetching image from:', fullUrl);

            const response = await api.get(fullUrl, {
                responseType: 'blob'
            });

            const imageObjectURL = URL.createObjectURL(response.data);
            setImageData(imageObjectURL);
        } catch (error) {
            console.error('❌ Failed to load image:', error);
        } finally {
            setImageLoading(false);
        }
    };

    const handleStatusUpdate = async (userId, newStatus, reason) => {
        try {
            await adminService.updateUserStatus({ userId, status: newStatus, reason });
            toast.success(`Đã cập nhật trạng thái người dùng thành ${getStatusLabel(newStatus)}`);
            fetchUserDetail();
            setShowStatusModal(false);
        } catch (error) {
            if (error.response?.status === 403) {
                toast.error('Không thể thay đổi trạng thái của tài khoản quản trị viên');
            } else {
                toast.error(error.response?.data || 'Không thể cập nhật trạng thái');
            }
            setShowStatusModal(false);
        }
    };

    const getStatusLabel = (statusValue) => {
        const statusMap = {
            ACTIVE: 'Đang hoạt động',
            INACTIVE: 'Không hoạt động',
            BANNED: 'Đã bị khóa',
            PENDING: 'Chờ xác nhận'
        };
        return statusMap[statusValue] || statusValue;
    };

    const getRoleLabel = (roleValue) => {
        const roleMap = {
            ADMIN: 'Quản trị viên',
            DOCTOR: 'Bác sĩ',
            PATIENT: 'Bệnh nhân'
        };
        return roleMap[roleValue] || roleValue;
    };

    const actionOptions = [
        { value: '', label: 'Tất cả', color: '' },
        { value: 'SET_INACTIVE', label: 'Không hoạt động', color: 'bg-gray-100 text-gray-700' },
        { value: 'BAN_USER', label: 'Khóa người dùng', color: 'bg-red-100 text-red-700' },
        { value: 'UNBAN_USER', label: 'Mở khóa người dùng', color: 'bg-green-100 text-green-700' },
        { value: 'CREATE_DOCTOR', label: 'Tạo bác sĩ', color: 'bg-purple-100 text-purple-700' },
        { value: 'CREATE_FINAL_DIAGNOSIS', label: 'Chẩn đoán cuối', color: 'bg-purple-100 text-purple-700' },
        { value: 'LOGIN', label: 'Đăng nhập', color: 'bg-green-100 text-green-700' },
        { value: 'LOGOUT', label: 'Đăng xuất', color: 'bg-gray-100 text-gray-700' },
    ];

    const getActionBadgeColor = (action) => {
        const found = actionOptions.find(opt => opt.value === action);
        return found?.color || 'bg-gray-100 text-gray-700';
    };

    const getActionLabel = (action) => {
        const found = actionOptions.find(opt => opt.value === action);
        return found?.label || action;
    };

    const formatDate = (date) => {
        if (!date) return '—';
        return new Date(date).toLocaleString('vi-VN');
    };

    const getAvatarColor = (role) => {
        switch (role) {
            case 'ADMIN':
                return 'bg-purple-100 text-purple-600';
            case 'DOCTOR':
                return 'bg-blue-100 text-blue-600';
            case 'PATIENT':
                return 'bg-green-100 text-green-600';
            default:
                return 'bg-gray-100 text-gray-600';
        }
    };

    const getFileType = (url) => {
        if (!url) return 'unknown';
        const ext = url.split('.').pop()?.toLowerCase();
        if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)) return 'image';
        if (['pdf'].includes(ext)) return 'pdf';
        return 'file';
    };

    const getFileIcon = (url) => {
        const type = getFileType(url);
        if (type === 'image') return <FaImage className="w-12 h-12 text-blue-500" />;
        if (type === 'pdf') return <FaFilePdf className="w-12 h-12 text-red-500" />;
        return <FaFile className="w-12 h-12 text-gray-500" />;
    };

    const getFileName = (url) => {
        if (!url) return 'Không có';
        const parts = url.split('/');
        return parts[parts.length - 1];
    };

    const getFullImageUrl = (url) => {
        if (!url) return null;
        if (url.startsWith('http://') || url.startsWith('https://')) {
            return url;
        }
        if (url.startsWith('/uploads/')) {
            return `${API_BASE_URL}${url}`;
        }
        return `${API_BASE_URL}/uploads/certificates/${url}`;
    };

    if (loading) return <LoadingSpinner />;
    if (!user) return null;

    const certificateUrl = user.certificateUrl || user.certificate || user.certificateFile;
    const isDoctor = user.roleName === 'DOCTOR';
    const fullImageUrl = getFullImageUrl(certificateUrl);
    const fileType = getFileType(certificateUrl);

    return (
        <div>
            <Topbar title="Chi tiết người dùng" />
            <div className="p-6">
                {/* Nút quay lại */}
                <button
                    onClick={() => navigate('/admin/users')}
                    className="flex items-center gap-2 text-gray-500 hover:text-[#100357] mb-4 transition"
                >
                    <FaArrowLeft className="w-4 h-4" /> Quay lại Quản lý người dùng
                </button>

                {/* Thẻ thông tin người dùng */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <div className={`w-16 h-16 ${getAvatarColor(user.roleName)} rounded-full flex items-center justify-center text-2xl font-bold`}>
                                {user.fullName?.charAt(0) || user.userName?.charAt(0) || 'U'}
                            </div>
                            <div>
                                <h2 className="text-2xl font-bold text-gray-800">{user.fullName}</h2>
                                <div className="flex gap-2 mt-2">
                                    <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                                        {getRoleLabel(user.roleName)}
                                    </span>
                                    <StatusBadge type="userStatus" value={user.status} />
                                </div>
                            </div>
                        </div>
                        {user.roleName !== 'ADMIN' && (
                            <button
                                onClick={() => setShowStatusModal(true)}
                                className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                            >
                                Cập nhật trạng thái
                            </button>
                        )}
                    </div>
                </div>

                {/* Thông tin cá nhân - Grid 2 cột */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <h3 className="font-semibold text-gray-800 mb-4">Thông tin cá nhân</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Tên đăng nhập</p>
                            <p className="text-gray-800 font-medium">{user.userName}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Email</p>
                            <p className="text-gray-800 font-medium">{user.email}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Số điện thoại</p>
                            <p className="text-gray-800 font-medium">{user.phoneNumber || '—'}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">CCCD/CMND</p>
                            <p className="text-gray-800 font-medium">{user.nationalId || '—'}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Ngày tạo</p>
                            <p className="text-gray-800 font-medium">
                                {formatDate(user.createdAt)}
                            </p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Lần cuối đổi mật khẩu</p>
                            <p className="text-gray-800 font-medium">
                                {formatDate(user.lastChangePassTime)}
                            </p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Lần cuối đăng xuất</p>
                            <p className="text-gray-800 font-medium">
                                {formatDate(user.lastLogoutTime)}
                            </p>
                        </div>
                        <div>
                            <p className="text-gray-400 text-xs uppercase tracking-wide">Mã người dùng</p>
                            <p className="text-gray-800 font-medium">{user.userId}</p>
                        </div>
                    </div>
                </div>

                {/* BẰNG CẤP - CHỈ HIỂN THỊ CHO DOCTOR */}
                {isDoctor && (
                    <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                        <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <FaGraduationCap className="text-[#100357]" />
                            Bằng cấp / Chứng chỉ
                        </h3>
                        {certificateUrl ? (
                            <div className="flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:shadow-md transition">
                                <div className="flex-shrink-0">
                                    {getFileIcon(certificateUrl)}
                                </div>
                                <div className="flex-1">
                                    <p className="font-medium text-gray-800">
                                        {getFileName(certificateUrl)}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        {fileType === 'image' ? 'Hình ảnh' :
                                            fileType === 'pdf' ? 'PDF' : 'File'}
                                    </p>
                                </div>
                                <button
                                    onClick={() => setShowCertificateModal(true)}
                                    className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition flex items-center gap-2"
                                >
                                    <FaImage className="w-4 h-4" />
                                    Xem
                                </button>
                            </div>
                        ) : (
                            <div className="text-center py-6 text-gray-400">
                                <FaGraduationCap className="w-12 h-12 mx-auto mb-2 text-gray-300" />
                                <p>Chưa có bằng cấp / chứng chỉ</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Nhật ký hoạt động gần đây */}
                <div className="bg-white rounded-lg shadow-sm p-6">
                    <h3 className="font-semibold text-gray-800 mb-4">Hoạt động gần đây</h3>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="border-b border-gray-200">
                            <tr className="text-left text-gray-500 text-sm">
                                <th className="pb-2">STT</th>
                                <th className="pb-2">Mã log</th>
                                <th className="pb-2">Thời gian</th>
                                <th className="pb-2">Hành động</th>
                                <th className="pb-2">Đối tượng</th>
                                <th className="pb-2">Mô tả</th>
                            </tr>
                            </thead>
                            <tbody>
                            {logs.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="py-4 text-center text-gray-400">
                                        Không có hoạt động nào
                                    </td>
                                </tr>
                            ) : (
                                logs.map((log, index) => (
                                    <tr key={log.logId} className="border-b border-gray-100 hover:bg-gray-50">
                                        <td className="py-2 text-sm text-gray-500">{index + 1}</td>
                                        <td className="py-2 text-sm">{log.logId}</td>
                                        <td className="py-2 text-sm">{formatDate(log.performedAt)}</td>
                                        <td className="py-2">
                                            <span className={`px-2 py-1 rounded-full text-xs font-medium whitespace-nowrap ${getActionBadgeColor(log.action)}`}>
                                                {getActionLabel(log.action)}
                                            </span>
                                        </td>
                                        <td className="py-2 text-sm">{log.targetType} #{log.targetId}</td>
                                        <td className="py-2 text-sm">{log.description}</td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Modal xem bằng cấp */}
                {showCertificateModal && (
                    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
                        <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-auto p-6">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                                    <FaGraduationCap className="text-[#100357]" />
                                    Bằng cấp / Chứng chỉ
                                </h3>
                                <button
                                    onClick={() => setShowCertificateModal(false)}
                                    className="text-gray-400 hover:text-gray-600"
                                >
                                    <FaTimes className="w-6 h-6" />
                                </button>
                            </div>
                            <div className="flex justify-center">
                                {fileType === 'image' ? (
                                    <div className="w-full flex justify-center">
                                        {imageLoading ? (
                                            <div className="text-center py-8">
                                                <div className="w-12 h-12 border-4 border-[#100357] border-t-transparent rounded-full animate-spin mx-auto"></div>
                                                <p className="text-gray-500 mt-2">Đang tải ảnh...</p>
                                            </div>
                                        ) : imageData ? (
                                            <img
                                                src={imageData}
                                                alt="Certificate"
                                                className="max-w-full max-h-[70vh] object-contain rounded-lg"
                                            />
                                        ) : (
                                            <div className="text-center p-8">
                                                <FaImage className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                                                <p className="text-gray-600">Không thể tải ảnh</p>
                                                <p className="text-sm text-gray-500">{getFileName(certificateUrl)}</p>
                                                <a
                                                    href={fullImageUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="inline-block mt-3 px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                                                >
                                                    Mở ảnh trong tab mới
                                                </a>
                                            </div>
                                        )}
                                    </div>
                                ) : fileType === 'pdf' ? (
                                    <div className="text-center p-8">
                                        <FaFilePdf className="w-24 h-24 text-red-500 mx-auto mb-4" />
                                        <p className="text-gray-600 mb-4">File PDF - Nhấn nút bên dưới để mở</p>
                                        <a
                                            href={fullImageUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="inline-block px-6 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                                        >
                                            Mở PDF
                                        </a>
                                    </div>
                                ) : (
                                    <div className="text-center p-8">
                                        <FaFile className="w-24 h-24 text-gray-500 mx-auto mb-4" />
                                        <p className="text-gray-600">Không thể xem trước file này</p>
                                        <a
                                            href={fullImageUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="inline-block mt-4 px-6 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                                        >
                                            Tải xuống
                                        </a>
                                    </div>
                                )}
                            </div>
                            <div className="mt-4 text-center text-sm text-gray-500">
                                <p>Tên file: {getFileName(certificateUrl)}</p>
                            </div>
                        </div>
                    </div>
                )}

                {/* Modal cập nhật trạng thái */}
                <UpdateStatusModal
                    isOpen={showStatusModal}
                    user={user}
                    onClose={() => setShowStatusModal(false)}
                    onConfirm={handleStatusUpdate}
                />
            </div>
        </div>
    );
};

export default UserDetail;