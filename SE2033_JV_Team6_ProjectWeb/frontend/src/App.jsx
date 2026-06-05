import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import AdminRoutes from './routes/AdminRoutes'
import PrivateRoute from './routes/PrivateRoute'
import Login from './pages/Login'

function App() {
    return (
        <Router>
            <AuthProvider>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/admin/*" element={
                        <PrivateRoute>
                            <AdminRoutes />
                        </PrivateRoute>
                    } />
                    <Route path="/" element={<Navigate to="/admin/dashboard" />} />
                    <Route path="*" element={<Navigate to="/admin/dashboard" />} />
                </Routes>
            </AuthProvider>
        </Router>
    )
}

export default App