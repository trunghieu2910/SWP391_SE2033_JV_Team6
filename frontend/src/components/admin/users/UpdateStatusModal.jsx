import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';

const UpdateStatusModal = ({ isOpen, user, onClose, onConfirm }) => {
    const [status, setStatus] = useState('');
    const [reason, setReason] = useState('');
    const [loading, setLoading] = useState(false);

    if (!isOpen || !user) return null;

    // Map trạng thái hiển thị cho dropdown
    const statusOptions = [
        { value: 'ACTIVE', label: 'Đang hoạt động', action: 'Kích hoạt tài khoản' },
        { value: 'INACTIVE', label: 'Không hoạt động', action: 'Chuyển sang không hoạt động' },
        { value: 'BANNED', label: 'Khoá tài khoản', action: 'Khoá tài khoản' },
    ];

    const getCurrentStatusLabel = () => {
        const option = statusOptions.find(opt => opt.value === user.status);
        return option ? option.label : user.status;
    };

    const getActionLabel = () => {
        const option = statusOptions.find(opt => opt.value === status);
        return option ? option.action : 'Cập nhật trạng thái';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!status) return;
        setLoading(true);
        await onConfirm(user.userId, status, reason);
        setLoading(false);
        setStatus('');
        setReason('');
    };

    if (!isOpen || !user) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg w-full max-w-md p-6">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-semibold text-gray-800">
                        {getActionLabel()}
                    </h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <FaTimes className="w-5 h-5" />
                    </button>
                </div>

                <p className="text-gray-600 mb-2">
                    Người dùng: <span className="font-medium">{user.fullName}</span> ({user.userName})
                </p>
                <p className="text-gray-500 text-sm mb-4">
                    Trạng thái hiện tại:
                    <span className="font-medium ml-1">
                        {getCurrentStatusLabel()}
                    </span>
                </p>

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-sm text-gray-600 mb-1">Trạng thái mới *</label>
                        <select
                            value={status}
                            onChange={(e) => setStatus(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                            required
                        >
                            <option value="">Chọn trạng thái</option>
                            {statusOptions.map(opt => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="mb-6">
                        <label className="block text-sm text-gray-600 mb-1">Lý do *</label>
                        <textarea
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            rows="3"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                            placeholder="Nhập lý do thay đổi trạng thái"
                            required
                        />
                    </div>

                    <div className="flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 bg-[#100357] text-white py-2 rounded-lg hover:bg-[#100357]/90 disabled:opacity-50"
                        >
                            {loading ? 'Đang cập nhật...' : 'Xác nhận'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UpdateStatusModal;