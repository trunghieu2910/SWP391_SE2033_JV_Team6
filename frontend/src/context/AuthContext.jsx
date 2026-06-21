import { createContext, useContext, useState, useEffect, useCallback } from 'react';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]                   = useState(null);
  const [token, setToken]                 = useState(null);
  const [isAuthenticated, setIsAuth]      = useState(false);
  const [isLoading, setIsLoading]         = useState(true);

  // Restore session from localStorage on mount
  useEffect(() => {
    const savedToken = localStorage.getItem('medai_token');
    const savedUser  = localStorage.getItem('medai_user');
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
        setIsAuth(true);
      } catch {
        localStorage.removeItem('medai_token');
        localStorage.removeItem('medai_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback((userData, accessToken) => {
    setUser(userData);
    setToken(accessToken);
    setIsAuth(true);
    localStorage.setItem('medai_token', accessToken);
    localStorage.setItem('medai_user', JSON.stringify(userData));
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    setIsAuth(false);
    localStorage.removeItem('medai_token');
    localStorage.removeItem('medai_user');
  }, []);

  return (
      <AuthContext.Provider value={{ user, token, isAuthenticated, isLoading, login, logout }}>
        {children}
      </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
