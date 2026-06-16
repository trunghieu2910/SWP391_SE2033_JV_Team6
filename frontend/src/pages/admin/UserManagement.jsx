import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSearch, FaFilter } from 'react-icons/fa';
import adminService from '../../services/adminService';
import Topbar from '../../components/admin/layout/Topbar';
import UserTable from '../../components/admin/users/UserTable';
import Pagination from '../../components/common/Pagination';
import UpdateStatusModal from '../../components/admin/users/UpdateStatusModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const UserManagement = () => {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({ keyword: '', role: '', status: '' });
    const [tempKeyword, setTempKeyword] = useState('');
    const [tempRole, setTempRole] = useState('');
    const [tempStatus, setTempStatus] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [selectedUser, setSelectedUser] = useState(null);
    const [showStatusModal, setShowStatusModal] = useState(false);

    useEffect(() => {
        fetchUsers();
    }, [filters, page]);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const params = { page, size: 10, ...filters };
            const response = await adminService.getUsers(params);
            setUsers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            toast.error('Không thể tải danh sách người dùng');
        } finally {
            setLoading(false);
        }
    };

    const performSearch = () => {
        setFilters({ keyword: tempKeyword, role: tempRole, status: tempStatus, page: 0 });
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') performSearch();
    };

    const handleStatusUpdate = async (userId, newStatus, reason) => {
        try {
            await adminService.updateUserStatus({ userId, status: newStatus, reason });
            toast.success(`Đã cập nhật trạng thái người dùng thành ${newStatus}`);
            fetchUsers();
            setShowStatusModal(false);
            setSelectedUser(null);
        } catch (error) {
            if (error.response?.status === 403) {
                toast.error('Không thể thay đổi trạng thái của tài khoản quản trị viên');
            } else if (error.response?.status === 400) {
                toast.error('Người dùng đã ở trạng thái bạn muốn thay đổi');
            } else {
                toast.error(error.response?.data || 'Không thể cập nhật trạng thái');
            }
            setShowStatusModal(false);
            setSelectedUser(null);
        }
    };

    const handleViewDetail = (userId) => navigate(`/admin/users/${userId}`);

    const handleClearFilters = () => {
        setTempKeyword('');
        setTempRole('');
        setTempStatus('');
        setFilters({ keyword: '', role: '', status: '' });
    };

    const roles = ['', 'ADMIN', 'DOCTOR', 'PATIENT'];
    const statuses = ['', 'ACTIVE', 'INACTIVE', 'BANNED'];

    if (loading) return <LoadingSpinner />;

    return (
        <div>
            <Topbar title="Quản lý người dùng" />
            <div className="p-6">
                <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-6">
                    <div className="flex flex-wrap gap-4 items-end">
                        <div className="flex-1 min-w-[200px]">
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Tìm kiếm</label>
                            <div className="relative">
                                <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="text"
                                    placeholder="Tên, email, số điện thoại..."
                                    value={tempKeyword}
                                    onChange={(e) => setTempKeyword(e.target.value)}
                                    onKeyDown={handleKeyDown}
                                    className="w-full pl-9 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition"
                                />
                            </div>
                        </div>
                        <div className="w-44">
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Vai trò</label>
                            <select
                                value={tempRole}
                                onChange={(e) => setTempRole(e.target.value)}
                                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition bg-white"
                            >
                                {roles.map(role => (
                                    <option key={role} value={role}>{role || 'Tất cả vai trò'}</option>
                                ))}
                            </select>
                        </div>
                        <div className="w-44">
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Trạng thái</label>
                            <select
                                value={tempStatus}
                                onChange={(e) => setTempStatus(e.target.value)}
                                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition bg-white"
                            >
                                {statuses.map(status => (
                                    <option key={status} value={status}>{status || 'Tất cả trạng thái'}</option>
                                ))}
                            </select>
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={performSearch}
                                className="px-5 py-2.5 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition flex items-center gap-2 shadow-sm"
                            >
                                <FaSearch className="w-4 h-4" /> Tìm kiếm
                            </button>
                            <button
                                onClick={handleClearFilters}
                                className="px-5 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 transition flex items-center gap-2"
                            >
                                <FaFilter className="w-4 h-4" /> Xóa bộ lọc
                            </button>
                        </div>
                    </div>
                </div>

                {/* Bảng người dùng */}
                <div className="mt-6">
                    <UserTable
                        users={users}
                        onViewDetail={handleViewDetail}
                        onUpdateStatus={(user) => {
                            setSelectedUser(user);
                            setShowStatusModal(true);
                        }}
                        startIndex={page * 10}
                    />
                </div>

                {totalPages > 1 && (
                    <div className="mt-4 flex justify-center">
                        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                    </div>
                )}

                <UpdateStatusModal
                    isOpen={showStatusModal}
                    user={selectedUser}
                    onClose={() => {
                        setShowStatusModal(false);
                        setSelectedUser(null);
                    }}
                    onConfirm={handleStatusUpdate}
                />
            </div>
        </div>
    );
};

export default UserManagement;