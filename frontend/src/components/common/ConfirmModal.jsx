import React from 'react';
import { FaTimes, FaExclamationTriangle } from 'react-icons/fa';

const ConfirmModal = ({ isOpen, title, message, confirmText = "Xác nhận", cancelText = "Hủy", onConfirm, onClose, isLoading = false }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg w-full max-w-md p-6 shadow-xl">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3 text-yellow-600">
                        <FaExclamationTriangle className="w-6 h-6" />
                        <h3 className="text-lg font-semibold text-gray-800">{title || "Xác nhận hành động"}</h3>
                    </div>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <FaTimes className="w-5 h-5" />
                    </button>
                </div>

                <div className="text-gray-600 mb-6">{message || "Bạn có chắc chắn muốn thực hiện hành động này?"}</div>

                <div className="flex gap-3">
                    <button
                        type="button"
                        onClick={onClose}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                        disabled={isLoading}
                    >
                        {cancelText}
                    </button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        className="flex-1 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition disabled:opacity-50"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Đang xử lý...' : confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;