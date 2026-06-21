import { StrictMode } from 'react';                     // Là công cụ hỗ trợ phát hiện lỗi trong quá trình phát triển (Development).
import { createRoot } from 'react-dom/client';          // Tạo React Root.
import App from './App.jsx';                            // Import component gốc.

// Bắt đầu render React.
createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App />
    </StrictMode>
);
