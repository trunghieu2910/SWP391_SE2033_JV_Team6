import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
})

// Request interceptor - thêm token giả
api.interceptors.request.use(
    (config) => {
        // Thêm token giả để backend không reject
        const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MX0.mock'
        config.headers.Authorization = `Bearer ${mockToken}`
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Response interceptor - xử lý lỗi
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error.response?.data || error.message)
        return Promise.reject(error)
    }
)

export default api