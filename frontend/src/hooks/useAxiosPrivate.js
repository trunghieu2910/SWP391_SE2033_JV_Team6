import { useEffect } from 'react';
import api from '../services/api';
import { useAuth } from './useAuth';

const useAxiosPrivate = () => {
    const { logout } = useAuth();

    useEffect(() => {
        const responseIntercept = api.interceptors.response.use(
            (response) => response,
            async (error) => {
                if (error.response?.status === 401) {
                    logout();
                }
                return Promise.reject(error);
            }
        );

        return () => {
            api.interceptors.response.eject(responseIntercept);
        };
    }, [logout]);

    return api;
};

export default useAxiosPrivate;