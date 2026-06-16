import React, { useEffect } from 'react';
import { FaCheckCircle, FaExclamationCircle, FaInfoCircle, FaTimes } from 'react-icons/fa';

const Toast = ({ type = 'success', message, onClose, duration = 3000 }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);
        return () => clearTimeout(timer);
    }, [duration, onClose]);

    const icons = {
        success: <FaCheckCircle className="w-5 h-5 text-green-500" />,
        error: <FaExclamationCircle className="w-5 h-5 text-red-500" />,
        info: <FaInfoCircle className="w-5 h-5 text-blue-500" />,
    };

    const bgColors = {
        success: 'bg-green-50 border-green-200',
        error: 'bg-red-50 border-red-200',
        info: 'bg-blue-50 border-blue-200',
    };

    return (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-lg border shadow-lg ${bgColors[type]}`}>
            {icons[type]}
            <span className="text-gray-700 text-sm">{message}</span>
            <button onClick={onClose} className="ml-2 text-gray-400 hover:text-gray-600">
                <FaTimes className="w-3 h-3" />
            </button>
        </div>
    );
};

export default Toast;