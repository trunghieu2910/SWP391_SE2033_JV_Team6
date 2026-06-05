import { useState, useEffect } from 'react'
import { adminService } from '../../services/adminService'
import DashboardStats from '../../components/admin/DashboardStats'
import RecentActivities from '../../components/admin/RecentActivities'
import LoadingSpinner from '../../components/admin/LoadingSpinner'

const Dashboard = () => {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        fetchDashboardStats()
    }, [])

    const fetchDashboardStats = async () => {
        try {
            setLoading(true)
            const response = await adminService.getDashboardStats()
            setStats(response.data)
        } catch (err) {
            setError('Failed to load dashboard statistics')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    if (loading) return <LoadingSpinner />
    if (error) return <div className="alert alert-danger">{error}</div>

    return (
        <div>
            <h2 className="mb-4">Dashboard</h2>
            <DashboardStats stats={stats} />
            <div className="mt-4">
                <RecentActivities />
            </div>
        </div>
    )
}

export default Dashboard