const TOKEN_KEY = 'access_token'

export const setToken = (token) => {
    localStorage.setItem(TOKEN_KEY, token || 'mock-token')
}

export const getToken = () => {
    // Trả về token giả nếu chưa có
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
        const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MX0.mock'
        localStorage.setItem(TOKEN_KEY, mockToken)
        return mockToken
    }
    return token
}

export const removeToken = () => {
    localStorage.removeItem(TOKEN_KEY)
}

export const decodeToken = (token) => {
    try {
        // Trả về mock user data
        return {
            sub: 'admin',
            role: 'ADMIN',
            userId: 1,
            exp: Date.now() + 24 * 60 * 60 * 1000
        }
    } catch (error) {
        console.error('Error decoding token:', error)
        return null
    }
}

export const isTokenValid = () => {
    // Luôn trả về true để bypass authentication
    return true
}