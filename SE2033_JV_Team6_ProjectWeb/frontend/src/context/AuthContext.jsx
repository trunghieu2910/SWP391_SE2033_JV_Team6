import React, { createContext, useState, useContext, useEffect } from 'react'
import { getToken, setToken, removeToken, decodeToken } from '../utils/tokenUtils'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = getToken()
    if (token) {
      const decoded = decodeToken(token)
      setUser(decoded)
    }
    setLoading(false)
  }, [])

  const login = (token) => {
    setToken(token)
    const decoded = decodeToken(token)
    setUser(decoded)
  }

  const logout = () => {
    removeToken()
    setUser(null)
  }

  return (
      <AuthContext.Provider value={{ user, login, logout, loading }}>
        {children}
      </AuthContext.Provider>
  )
}