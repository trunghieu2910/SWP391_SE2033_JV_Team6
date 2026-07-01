import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    FaUsers,
    FaUserMd,
    FaUserInjured,
    FaBan,
    FaChartLine,
    FaEye
} from 'react-icons/fa';
import {
    LineChart,
    Line,
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend
} from 'recharts';
import adminService from '../../services/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatsCard from '../../components/admin/dashboard/StatsCard';
import GlobalSearch from '../../components/admin/common/GlobalSearch';
import toast from 'react-hot-toast';

const Dashboard = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [recentLogs, setRecentLogs] = useState([]);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const [statsRes, chartsRes, logsRes] = await Promise.all([
                adminService.getDashboardStats(),
                adminService.getChartStats(),
                adminService.getSystemLogs({ page: 0, size: 5 })
            ]);

            setStats(statsRes.data);
            setRecentLogs(logsRes.data.content || []);

            const userData = chartsRes.data.userRegistrations || [];
            const sessionData = chartsRes.data.diagnosisSessions || [];

            const mergedData = userData.map((item, index) => ({
                month: formatMonth(item.month),
                users: item.count,
                sessions: sessionData[index]?.count || 0
            }));

            setChartData(mergedData);
        } catch (error) {
            toast.error('Không thể tải dữ liệu tổng quan');
            console.error('Dashboard error:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatMonth = (monthStr) => {
        if (!monthStr) return '';
        const [year, month] = monthStr.split('-');
        return `Thg ${parseInt(month)}`;
    };

    const actionOptions = [
        { value: '', label: 'Tất cả', color: '' },
        { value: 'SET_INACTIVE', label: 'Không hoạt động', color: 'bg-gray-100 text-gray-700' },
        { value: 'BAN_USER', label: 'Khóa người dùng', color: 'bg-red-100 text-red-700' },
        { value: 'UNBAN_USER', label: 'Mở khóa người dùng', color: 'bg-green-100 text-green-700' },
        { value: 'CREATE_DOCTOR', label: 'Tạo bác sĩ', color: 'bg-purple-100 text-purple-700' },
        { value: 'CREATE_FINAL_DIAGNOSIS', label: 'Chẩn đoán cuối', color: 'bg-purple-100 text-purple-700' },
        { value: 'LOGIN', label: 'Đăng nhập', color: 'bg-green-100 text-green-700' },
        { value: 'LOGOUT', label: 'Đăng xuất', color: 'bg-gray-100 text-gray-700' },
    ];

    const getActionBadgeColor = (action) => {
        const found = actionOptions.find(opt => opt.value === action);
        return found?.color || 'bg-gray-100 text-gray-700';
    };

    const getActionLabel = (action) => {
        const found = actionOptions.find(opt => opt.value === action);
        return found?.label || action;
    };

    if (loading) return <LoadingSpinner />;

    return (
        <div>
            {/* ✅ Đưa Global Search vào Topbar bằng cách sửa Topbar hoặc tạo header riêng */}
            <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-800">Tổng quan</h1>
                <GlobalSearch />
            </div>

            <div className="p-6">
                {/* Thống kê */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
                    <StatsCard
                        title="Tổng người dùng"
                        value={stats?.totalUsers}
                        icon={FaUsers}
                        color="#3B82F6"
                    />
                    <StatsCard
                        title="Tổng bác sĩ"
                        value={stats?.totalDoctors}
                        icon={FaUserMd}
                        color="#10B981"
                    />
                    <StatsCard
                        title="Tổng bệnh nhân"
                        value={stats?.totalPatients}
                        icon={FaUserInjured}
                        color="#8B5CF6"
                    />
                    <StatsCard
                        title="Người dùng bị khóa"
                        value={stats?.blockedUsers}
                        icon={FaBan}
                        color="#EF4444"
                    />
                    <StatsCard
                        title="Phiên chẩn đoán"
                        value={stats?.totalDiagnosisSessions}
                        icon={FaChartLine}
                        color="#F59E0B"
                    />
                </div>

                {/* Biểu đồ */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                    <div className="bg-white rounded-lg shadow-sm p-4">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="font-semibold text-gray-800">Đăng ký người dùng theo thời gian</h3>
                            <button
                                onClick={() => navigate('/admin/users')}
                                className="text-[#100357] hover:text-[#100357]/80 text-sm flex items-center gap-1"
                            >
                                <FaEye className="w-3 h-3" /> Xem tất cả
                            </button>
                        </div>
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="month" />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Line type="monotone" dataKey="users" stroke="#100357" strokeWidth={2} name="Người dùng" />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>

                    <div className="bg-white rounded-lg shadow-sm p-4">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="font-semibold text-gray-800">Xu hướng phiên chẩn đoán</h3>
                            <button
                                onClick={() => navigate('/admin/sessions')}
                                className="text-[#100357] hover:text-[#100357]/80 text-sm flex items-center gap-1"
                            >
                                <FaEye className="w-3 h-3" /> Xem tất cả
                            </button>
                        </div>
                        <ResponsiveContainer width="100%" height={300}>
                            <AreaChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="month" />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Area type="monotone" dataKey="sessions" fill="#100357" fillOpacity={0.3} stroke="#100357" name="Phiên chẩn đoán" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Nhật ký hoạt động gần đây */}
                <div className="bg-white rounded-lg shadow-sm p-4">
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="font-semibold text-gray-800">Nhật ký hoạt động gần đây</h3>
                        <button
                            onClick={() => navigate('/admin/logs')}
                            className="text-[#100357] hover:text-[#100357]/80 text-sm flex items-center gap-1"
                        >
                            Xem tất cả →
                        </button>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="border-b border-gray-200">
                            <tr className="text-left text-gray-500 text-sm">
                                <th className="pb-2">Thời gian</th>
                                <th className="pb-2">Hành động</th>
                                <th className="pb-2">Mô tả</th>
                                <th className="pb-2">Người dùng</th>
                            </tr>
                            </thead>
                            <tbody>
                            {recentLogs.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="py-4 text-center text-gray-400">
                                        Không có nhật ký hoạt động
                                    </td>
                                </tr>
                            ) : (
                                recentLogs.map((log, idx) => (
                                    <tr key={idx} className="border-b border-gray-100 hover:bg-gray-50">
                                        <td className="py-2 text-sm">
                                            {new Date(log.performedAt).toLocaleString()}
                                        </td>
                                        <td className="py-2">
                                            <span className={`px-2 py-1 rounded-full text-xs font-medium whitespace-nowrap ${getActionBadgeColor(log.action)}`}>
                                                {getActionLabel(log.action)}
                                            </span>
                                        </td>
                                        <td className="py-2 text-sm">{log.description}</td>
                                        <td className="py-2 text-sm">{log.user?.username || 'Hệ thống'}</td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;