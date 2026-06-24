import { BrowserRouter } from 'react-router-dom';               // Kích hoạt chức năng điều hướng (routing) trong React
import { AuthProvider }  from './context/AuthContext';          // Cung cấp dữ liệu đăng nhập cho toàn bộ ứng dụng.
import AppRoutes         from './routes/AppRoutes';             // Quản lý tất cả URL của ứng dụng.
import './index.css';                                           // Load CSS toàn cục.

export default function App() {                                 // Tạo component gốc tên: App
  return (
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
  );
}
