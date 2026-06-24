import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSearch, FaEye, FaFilter, FaSpinner } from 'react-icons/fa';
import doctorService from '../../services/doctorService';
import Pagination from '../../components/common/Pagination';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ConfirmModal from '../../components/common/ConfirmModal';
import toast from 'react-hot-toast';

const DoctorDashboard = () => {
    const navigate = useNavigate();
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updatingId, setUpdatingId] = useState(null);
    const [filters, setFilters] = useState({ keyword: '', status: '' });
    const [tempKeyword, setTempKeyword] = useState('');
    const [tempStatus, setTempStatus] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedSession, setSelectedSession] = useState(null);
    const [selectedNewStatus, setSelectedNewStatus] = useState('');
    const [showShareModal, setShowShareModal] = useState(false);
    const [selectedShareSession, setSelectedShareSession] = useState(null);

    const statusOptions = [
        { value: 'PENDING', label: 'Chờ xử lý', color: 'bg-yellow-100 text-yellow-700' },
        { value: 'PROCESSING', label: 'Đang xử lý', color: 'bg-blue-100 text-blue-700' },
        { value: 'COMPLETED', label: 'Hoàn thành', color: 'bg-green-100 text-green-700' },
        { value: 'FAILED', label: 'Thất bại', color: 'bg-red-100 text-red-700' },
    ];

    useEffect(() => {
        fetchSessions();
    }, [filters, page]);

    const fetchSessions = async () => {
        setLoading(true);
        try {
            const params = { page, size: 10, sortBy: 'createdAt', sortDir: 'asc', ...filters };
            const response = await doctorService.getSessions(params);
            setSessions(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            toast.error('Không thể tải danh sách ca chẩn đoán');
        } finally {
            setLoading(false);
        }
    };

    const performSearch = () => {
        setFilters({ keyword: tempKeyword, status: tempStatus, page: 0 });
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') performSearch();
    };

    const handleClearFilters = () => {
        setTempKeyword('');
        setTempStatus('');
        setFilters({ keyword: '', status: '' });
    };

    const openStatusModal = (session) => {
        setSelectedSession(session);
        setSelectedNewStatus('');
        setShowStatusModal(true);
    };

    const confirmStatusUpdate = async () => {
        if (!selectedSession || !selectedNewStatus) return;

        setUpdatingId(selectedSession.sessionId);
        try {
            await doctorService.updateSessionStatus({
                sessionId: selectedSession.sessionId,
                status: selectedNewStatus
            });
            toast.success(`Đã chuyển trạng thái sang ${getStatusLabel(selectedNewStatus)}`);
            fetchSessions();
        } catch (error) {
            toast.error('Không thể cập nhật trạng thái khi đã công bố');
        } finally {
            setUpdatingId(null);
            setShowStatusModal(false);
            setSelectedSession(null);
            setSelectedNewStatus('');
        }
    };

    const openShareModal = (session) => {
        const status = session.status?.toString().trim().toUpperCase() || '';

        if (status !== 'COMPLETED') {
            toast.error('Chỉ có thể công bố khi ca chẩn đoán có trạng thái "Hoàn thành"');
            return;
        }
        setSelectedShareSession(session);
        setShowShareModal(true);
    };

    const confirmShareUpdate = async () => {
        if (!selectedShareSession) return;

        setUpdatingId(selectedShareSession.sessionId);
        try {
            await doctorService.updateSessionShare({
                sessionId: selectedShareSession.sessionId,
                isShared: !selectedShareSession.isShared
            });
            toast.success(selectedShareSession.isShared ? 'Đã gỡ công bố' : 'Đã công bố ca chẩn đoán');
            fetchSessions();
        } catch (error) {
            toast.error('Không thể cập nhật trạng thái công bố');
        } finally {
            setUpdatingId(null);
            setShowShareModal(false);
            setSelectedShareSession(null);
        }
    };

    const handleViewDetail = (sessionId) => {
        navigate(`/doctor/sessions/${sessionId}`);
    };

    const getStatusLabel = (statusValue) => {
        const option = statusOptions.find(opt => opt.value === statusValue);
        return option ? option.label : statusValue;
    };

    if (loading) return <LoadingSpinner />;

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Quản lý ca chẩn đoán</h1>

            {/* Vùng tìm kiếm - Bo tròn và rõ vùng */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-6">
                <div className="flex flex-wrap gap-4 items-end">
                    <div className="flex-1 min-w-[200px]">
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Tìm kiếm</label>
                        <div className="relative">
                            <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                            <input
                                type="text"
                                placeholder="Tên bệnh nhân..."
                                value={tempKeyword}
                                onChange={(e) => setTempKeyword(e.target.value)}
                                onKeyDown={handleKeyDown}
                                className="w-full pl-9 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition"
                            />
                        </div>
                    </div>
                    <div className="w-48">
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Trạng thái</label>
                        <select
                            value={tempStatus}
                            onChange={(e) => setTempStatus(e.target.value)}
                            className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition bg-white"
                        >
                            <option value="">Tất cả</option>
                            {statusOptions.map(opt => (
                                <option key={opt.value} value={opt.value}>{opt.label}</option>
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

            {/* Bảng danh sách - Bo tròn và rõ vùng */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-gray-50 rounded-t-xl">
                            <tr className="text-left text-gray-600 text-sm">
                                <th className="px-4 py-3 rounded-tl-xl">STT</th>
                                <th className="px-4 py-3">ID</th>
                                <th className="px-4 py-3">Tên bệnh nhân</th>
                                <th className="px-4 py-3">Thời gian</th>
                                <th className="px-4 py-3">Trạng thái</th>
                                <th className="px-4 py-3">Công bố</th>
                                <th className="px-4 py-3 rounded-tr-xl">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                        {sessions.length === 0 ? (
                            <tr>
                                <td colSpan="7" className="px-4 py-8 text-center text-gray-400">
                                    Không có ca chẩn đoán nào
                                </td>
                            </tr>
                        ) : (
                            sessions.map((session, index) => (
                                <tr key={session.sessionId} className="border-b border-gray-100 hover:bg-gray-50 transition">
                                    <td className="px-4 py-3 text-sm text-gray-500">{page * 10 + index + 1}</td>
                                    <td className="px-4 py-3 text-sm">{session.sessionId}</td>
                                    <td className="px-4 py-3 text-sm font-medium">{session.fullName}</td>
                                    <td className="px-4 py-3 text-sm">
                                        {new Date(session.createdAt).toLocaleString()}
                                    </td>
                                    <td className="px-4 py-3">
                                        <button
                                            onClick={() => openStatusModal(session)}
                                            className="hover:opacity-80 transition"
                                            disabled={updatingId === session.sessionId}
                                        >
                                            <StatusBadge type="status" value={session.status} />
                                        </button>
                                    </td>
                                    <td className="px-4 py-3">
                                        <button
                                            onClick={() => openShareModal(session)}
                                            disabled={updatingId === session.sessionId}
                                            className={`px-3 py-1 rounded-full text-xs font-medium transition ${
                                                session.isShared
                                                    ? 'bg-green-100 text-green-700 hover:bg-green-200'
                                                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                            }`}
                                        >
                                            {updatingId === session.sessionId ? (
                                                <FaSpinner className="w-3 h-3 animate-spin inline" />
                                            ) : (
                                                session.isShared ? 'Đã công bố' : 'Chưa công bố'
                                            )}
                                        </button>
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="flex gap-2">
                                            <button
                                                onClick={() => handleViewDetail(session.sessionId)}
                                                className="p-1 text-gray-500 hover:text-[#100357] transition"
                                                title="Xem chi tiết"
                                            >
                                                <FaEye className="w-4 h-4" />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
                {totalPages > 1 && (
                    <div className="p-4 border-t border-gray-200">
                        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                    </div>
                )}
            </div>

            {/* Modal xác nhận đổi trạng thái */}
            <ConfirmModal
                isOpen={showStatusModal}
                title="Xác nhận đổi trạng thái"
                message={
                    <div>
                        <p className="mb-3">Bạn có chắc chắn muốn đổi trạng thái của ca chẩn đoán <strong>#{selectedSession?.sessionId}</strong>?</p>
                        <div className="mt-3">
                            <label className="block text-sm text-gray-600 mb-2">Chọn trạng thái mới:</label>
                            <select
                                value={selectedNewStatus}
                                onChange={(e) => setSelectedNewStatus(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                            >
                                <option value="">Chọn trạng thái</option>
                                {statusOptions.map(opt => (
                                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                                ))}
                            </select>
                        </div>
                    </div>
                }
                confirmText="Xác nhận"
                cancelText="Hủy"
                onConfirm={confirmStatusUpdate}
                onClose={() => {
                    setShowStatusModal(false);
                    setSelectedSession(null);
                    setSelectedNewStatus('');
                }}
                isLoading={updatingId !== null}
            />

            {/* Modal xác nhận đổi công bố */}
            <ConfirmModal
                isOpen={showShareModal}
                title="Xác nhận thay đổi công bố"
                message={`Bạn có chắc chắn muốn ${selectedShareSession?.isShared ? 'gỡ công bố' : 'công bố'} ca chẩn đoán #${selectedShareSession?.sessionId}?`}
                confirmText={selectedShareSession?.isShared ? 'Gỡ công bố' : 'Công bố'}
                cancelText="Hủy"
                onConfirm={confirmShareUpdate}
                onClose={() => {
                    setShowShareModal(false);
                    setSelectedShareSession(null);
                }}
                isLoading={updatingId !== null}
            />
        </div>
);
};

export default DoctorDashboard;