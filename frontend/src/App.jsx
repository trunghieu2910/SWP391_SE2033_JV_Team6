import React, { useEffect } from 'react';
import { AuthProvider } from './contexts/AuthContext';
import AppRoutes from './routes/AppRoutes';
import { useToast, ToastContainer } from './components/Toast';
import { registerToastEmitter } from './services/api';
import './index.css';

export default function App() {
    const { toasts, addToast, removeToast } = useToast();

    useEffect(() => {
        // Đăng ký hàm hiển thị toast cho api.js để kích hoạt khi gặp lỗi 429 hoặc 403
        registerToastEmitter(addToast);
    }, [addToast]);

    return (
        <AuthProvider>
            <ToastContainer toasts={toasts} onRemove={removeToast} />
            <AppRoutes />
        </AuthProvider>
    );
}