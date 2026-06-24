import { Navigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/admin/common/LoadingSpinner';

export function AdminRoute({ children }) {
    const { isAuthenticated, isLoading, user } = useAuth();

    if (isLoading) {
        return <LoadingSpinner />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    const isAdmin = user?.role === 'ADMIN';
    return isAdmin ? children : <Navigate to="/unauthorized" replace />;
}