import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft, FaUserCircle } from 'react-icons/fa';
import adminService from '../../services/adminService';
import Topbar from '../../components/admin/layout/Topbar';
import StatusBadge from '../../components/common/StatusBadge';
import UpdateStatusModal from '../../components/admin/users/UpdateStatusModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const UserDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showStatusModal, setShowStatusModal] = useState(false);

    useEffect(() => {
        fetchUserDetail();
    }, [id]);

    const fetchUserDetail = async () => {
        setLoading(true);
        try {
            const response = await adminService.getUserDetail(id);
            console.log('📦 Dữ liệu người dùng:', response.data.userResponse);
            setUser(response.data.userResponse);
            setLogs(response.data.systemLogResponses || []);
        } catch (error) {
            toast.error('Không thể tải thông tin người dùng');
            navigate('/admin/users');
        } finally {
            setLoading(false);
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

    if (loading) return <LoadingSpinner />;
    if (!user) return null;

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