import { Navigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/admin/common/LoadingSpinner';

export function AdminRoute({ children }) {
    const { isAuthenticated, isAdmin, loading } = useAuth();

    if (loading) {
        return <LoadingSpinner />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return isAdmin ? children : <Navigate to="/unauthorized" replace />;
}