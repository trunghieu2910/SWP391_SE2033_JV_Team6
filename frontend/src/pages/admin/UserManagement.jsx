import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { Search, Filter, Eye, Ban, Unlock, ChevronLeft, ChevronRight, UserCheck, UserX, Users, Calendar, Mail, Shield } from 'lucide-react';
import adminService from '../../services/adminService';
import UserDetailModal from '../../components/UserDetailModal';
import ConfirmModal from '../../components/ConfirmModal';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [filters, setFilters] = useState({ keyword: '', role: '', status: '' });
  const [selectedUser, setSelectedUser] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [userToToggle, setUserToToggle] = useState(null);

  useEffect(() => {
    fetchUsers();
  }, [currentPage, filters.role, filters.status]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await adminService.getUsers({
        page: currentPage,
        size: 10,
        ...filters
      });
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      toast.error('Không thể tải danh sách người dùng');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchUsers();
  };

  const handleViewDetail = async (userId) => {
    try {
      const data = await adminService.getUserDetail(userId);
      setSelectedUser(data);
      setShowDetailModal(true);
    } catch (error) {
      toast.error('Không thể tải thông tin chi tiết');
    }
  };

  const handleToggleStatus = (user) => {
    setUserToToggle(user);
    setShowConfirmModal(true);
  };

  const confirmToggleStatus = async () => {
    if (!userToToggle) return;

    const newStatus = userToToggle.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
    try {
      await adminService.updateUserStatus(userToToggle.userId, newStatus);
      toast.success(`Đã ${newStatus === 'BANNED' ? 'khóa' : 'mở khóa'} tài khoản ${userToToggle.fullName}`);
      fetchUsers();
      if (selectedUser?.userResponse.userId === userToToggle.userId) {
        setSelectedUser(null);
        setShowDetailModal(false);
      }
    } catch (error) {
      toast.error('Cập nhật trạng thái thất bại');
    } finally {
      setShowConfirmModal(false);
      setUserToToggle(null);
    }
  };

  const getStatusBadge = (status) => {
    return status === 'ACTIVE'
        ? 'bg-green-100 text-green-800'
        : 'bg-red-100 text-red-800';
  };

  const getRoleBadge = (role) => {
    const styles = {
      ADMIN: 'bg-purple-100 text-purple-800',
      DOCTOR: 'bg-blue-100 text-blue-800',
      PATIENT: 'bg-gray-100 text-gray-800'
    };
    return styles[role] || 'bg-gray-100 text-gray-800';
  };

  return (
      <div className="p-6">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Quản Lý Người Dùng</h1>
          <p className="text-gray-600 mt-1">Quản lý và giám sát tất cả người dùng trong hệ thống</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Tổng người dùng</p>
                <p className="text-2xl font-bold text-gray-900">{users.length}</p>
              </div>
              <Users className="w-10 h-10 text-primary-500 opacity-75" />
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Đang hoạt động</p>
                <p className="text-2xl font-bold text-green-600">
                  {users.filter(u => u.status === 'ACTIVE').length}
                </p>
              </div>
              <UserCheck className="w-10 h-10 text-green-500 opacity-75" />
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Đã khóa</p>
                <p className="text-2xl font-bold text-red-600">
                  {users.filter(u => u.status === 'BANNED').length}
                </p>
              </div>
              <UserX className="w-10 h-10 text-red-500 opacity-75" />
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm">Bác sĩ</p>
                <p className="text-2xl font-bold text-blue-600">
                  {users.filter(u => u.roleName === 'DOCTOR').length}
                </p>
              </div>
              <Shield className="w-10 h-10 text-blue-500 opacity-75" />
            </div>
          </div>
        </div>

        {/* Search and Filter Bar */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <form onSubmit={handleSearch} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                    type="text"
                    placeholder="Tìm kiếm theo tên, email..."
                    value={filters.keyword}
                    onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
              <select
                  value={filters.role}
                  onChange={(e) => setFilters({ ...filters, role: e.target.value })}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="">Tất cả vai trò</option>
                <option value="ADMIN">Admin</option>
                <option value="DOCTOR">Bác sĩ</option>
                <option value="PATIENT">Bệnh nhân</option>
              </select>
              <select
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE">Hoạt động</option>
                <option value="BANNED">Đã khóa</option>
              </select>
            </div>
            <div className="flex justify-end">
              <button type="submit" className="btn-primary flex items-center gap-2">
                <Search className="w-4 h-4" />
                Tìm kiếm
              </button>
            </div>
          </form>
        </div>

        {/* Users Table */}
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Người dùng</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vai trò</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngày tạo</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
              </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
              {loading ? (
                  <tr>
                    <td colSpan="7" className="px-6 py-12 text-center">
                      <div className="flex justify-center">
                        <div className="w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full animate-spin" />
                      </div>
                    </td>
                  </tr>
              ) : users.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="px-6 py-12 text-center text-gray-500">
                      Không tìm thấy người dùng nào
                    </td>
                  </tr>
              ) : (
                  users.map((user) => (
                      <tr key={user.userId} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{user.userId}</td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div>
                            <div className="text-sm font-medium text-gray-900">{user.fullName || user.userName}</div>
                            <div className="text-sm text-gray-500">@{user.userName}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{user.email}</td>
                        <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getRoleBadge(user.roleName)}`}>
                        {user.roleName}
                      </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusBadge(user.status)}`}>
                        {user.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}
                      </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(user.createdAt).toLocaleDateString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                                onClick={() => handleViewDetail(user.userId)}
                                className="p-1 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                title="Xem chi tiết"
                            >
                              <Eye className="w-5 h-5" />
                            </button>
                            {user.roleName !== 'ADMIN' && (
                                <button
                                    onClick={() => handleToggleStatus(user)}
                                    className={`p-1 rounded-lg transition-colors ${
                                        user.status === 'ACTIVE'
                                            ? 'text-red-600 hover:bg-red-50'
                                            : 'text-green-600 hover:bg-green-50'
                                    }`}
                                    title={user.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                                >
                                  {user.status === 'ACTIVE' ? <Ban className="w-5 h-5" /> : <Unlock className="w-5 h-5" />}
                                </button>
                            )}
                          </div>
                        </td>
                      </tr>
                  ))
              )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
              <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
                <button
                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                    disabled={currentPage === 0}
                    className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronLeft className="w-5 h-5" />
                </button>
                <span className="text-sm text-gray-600">
              Trang {currentPage + 1} / {totalPages}
            </span>
                <button
                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                    disabled={currentPage === totalPages - 1}
                    className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronRight className="w-5 h-5" />
                </button>
              </div>
          )}
        </div>

        {/* Modals */}
        {showDetailModal && selectedUser && (
            <UserDetailModal user={selectedUser} onClose={() => setShowDetailModal(false)} onToggleStatus={handleToggleStatus} />
        )}

        {showConfirmModal && userToToggle && (
            <ConfirmModal
                title={userToToggle.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                message={`Bạn có chắc chắn muốn ${userToToggle.status === 'ACTIVE' ? 'khóa' : 'mở khóa'} tài khoản của ${userToToggle.fullName}?`}
                onConfirm={confirmToggleStatus}
                onCancel={() => setShowConfirmModal(false)}
            />
        )}
      </div>
  );
};

export default UserManagement;