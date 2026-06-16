import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import doctorService from '../../../services/doctorService';
import toast from 'react-hot-toast';

const AddLabResultModal = ({ isOpen, onClose, sessionId, onSuccess }) => {
    const [loading, setLoading] = useState(false);
    const [testType, setTestType] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!testType.trim()) {
            toast.error('Vui lòng chọn loại xét nghiệm');
            return;
        }

        setLoading(true);
        try {
            const payload = {
                sessionId: parseInt(sessionId),
                testType: testType,
                parameters: []
            };

            await doctorService.createLabResult(payload);
            toast.success('Thêm xét nghiệm thành công');
            setTestType('');
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Create lab result error:', error);
            toast.error(error.response?.data?.message || 'Không thể thêm xét nghiệm');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setTestType('');
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl w-full max-w-md">
                <div className="flex items-center justify-between p-4 border-b border-gray-200">
                    <h3 className="text-lg font-semibold text-gray-800">Thêm xét nghiệm mới</h3>
                    <button onClick={handleClose} className="text-gray-400 hover:text-gray-600">
                        <FaTimes className="w-5 h-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Loại xét nghiệm <span className="text-red-500">*</span>
                        </label>
                        <select
                            value={testType}
                            onChange={(e) => setTestType(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent"
                            autoFocus
                        >
                            <option value="" disabled>Chọn loại xét nghiệm</option>
                            <option value="Xét nghiệm tế bào (PAP)">Xét nghiệm tế bào (PAP)</option>
                            <option value="Xét nghiệm HPV DNA">Xét nghiệm HPV DNA</option>
                            <option value="Xét nghiệm sinh thiết (Biopsy)">Xét nghiệm sinh thiết (Biopsy)</option>
                            <option value="Xét nghiệm máu">Xét nghiệm máu</option>
                            <option value="Xét nghiệm tổng quát">Xét nghiệm tổng quát</option>
                        </select>
                    </div>

                    <div className="flex gap-3 mt-6 pt-4 border-t border-gray-200">
                        <button
                            type="button"
                            onClick={handleClose}
                            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 bg-[#100357] text-white py-2 rounded-lg hover:bg-[#100357]/90 disabled:opacity-50 transition"
                        >
                            {loading ? 'Đang thêm...' : 'Thêm xét nghiệm'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddLabResultModal;