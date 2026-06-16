import React, { useState, useEffect } from 'react';
import { FaSearch, FaDownload, FaFilter } from 'react-icons/fa';
import adminService from '../../services/adminService';
import Topbar from '../../components/admin/layout/Topbar';
import LogsTable from '../../components/admin/logs/LogsTable';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const SystemLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({ action: '', keyword: '' });
    const [tempAction, setTempAction] = useState('');
    const [tempKeyword, setTempKeyword] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchLogs();
    }, [filters, page]);

    const fetchLogs = async () => {
        setLoading(true);
        try {
            const params = { page, size: 10, ...filters };
            const response = await adminService.getSystemLogs(params);
            setLogs(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            toast.error('Không thể tải nhật ký hệ thống');
        } finally {
            setLoading(false);
        }
    };

    const performSearch = () => {
        setFilters({ action: tempAction, keyword: tempKeyword, page: 0 });
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') performSearch();
    };

    const exportCSV = () => {
        const headers = ['Mã log', 'Thời gian', 'Hành động', 'Loại đối tượng', 'ID đối tượng', 'Mô tả', 'Người dùng'];
        const rows = logs.map(log => [
            log.logId,
            new Date(log.performedAt).toLocaleString(),
            log.action,
            log.targetType,
            log.targetId,
            log.description,
            log.user?.username || 'Hệ thống'
        ]);
        const csvContent = [headers, ...rows].map(row => row.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `he_thong_log_${new Date().toISOString()}.csv`;
        a.click();
        URL.revokeObjectURL(url);
        toast.success('Đang xuất file...');
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

    const handleClearFilters = () => {
        setTempAction('');
        setTempKeyword('');
        setFilters({ action: '', keyword: '' });
    };

    if (loading) return <LoadingSpinner />;

    return (
        <div>
            <Topbar title="Nhật ký hệ thống" />
            <div className="p-6">
                {/* Vùng tìm kiếm - Bo tròn và đẹp hơn */}
                <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-6">
                    <div className="flex flex-wrap gap-4 items-end">
                        <div className="w-56">
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Hành động</label>
                            <select
                                value={tempAction}
                                onChange={(e) => setTempAction(e.target.value)}
                                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition bg-white"
                            >
                                {actionOptions.map(opt => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="flex-1 min-w-[200px]">
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Tìm kiếm</label>
                            <div className="relative">
                                <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="text"
                                    placeholder="Từ khóa..."
                                    value={tempKeyword}
                                    onChange={(e) => setTempKeyword(e.target.value)}
                                    onKeyDown={handleKeyDown}
                                    className="w-full pl-9 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition"
                                />
                            </div>
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
                            <button
                                onClick={exportCSV}
                                className="px-5 py-2.5 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition flex items-center gap-2 shadow-sm"
                            >
                                <FaDownload className="w-4 h-4" /> Xuất CSV
                            </button>
                        </div>
                    </div>
                </div>

                <div className="mt-6">
                    <LogsTable logs={logs} startIndex={page * 10} />
                </div>

                {totalPages > 1 && (
                    <div className="mt-4 flex justify-center">
                        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default SystemLogs;