import { Navigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/admin/common/LoadingSpinner';

export function PrivateRoute({ children }) {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return <LoadingSpinner />;
    }

    return isAuthenticated ? children : <Navigate to="/login" replace />;
}