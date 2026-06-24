import React, { useState } from 'react';
import { FaRobot, FaSpinner } from 'react-icons/fa';
import toast from 'react-hot-toast';

const AIAnalysisButton = ({ sessionId }) => {
    const [loading, setLoading] = useState(false);

    const handleAIAnalysis = async () => {
        setLoading(true);
        // Simulate API call - sẽ thay bằng API thật sau
        setTimeout(() => {
            toast.info('Tính năng phân tích từ AI đang được phát triển. Vui lòng quay lại sau!');
            setLoading(false);
        }, 1000);
    };

    return (
        <div className="mt-6 pt-4 border-t border-gray-200">
            <button
                onClick={handleAIAnalysis}
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-[#100357] to-[#2a1a6e] text-white rounded-lg font-medium hover:opacity-90 transition flex items-center justify-center gap-2"
            >
                {loading ? (
                    <FaSpinner className="w-5 h-5 animate-spin" />
                ) : (
                    <FaRobot className="w-5 h-5" />
                )}
                {loading ? 'Đang phân tích...' : 'Hỗ trợ phân tích từ AI'}
            </button>
            <p className="text-xs text-gray-400 text-center mt-2">
                AI sẽ phân tích tổng hợp dữ liệu để đề xuất chẩn đoán
            </p>
        </div>
    );
};

export default AIAnalysisButton;