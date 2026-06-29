import React, { useState, useEffect } from 'react';
import { securityService } from '../../services/securityService';
import { Shield, ShieldAlert, ShieldCheck, Trash2, Ban, RefreshCw } from 'lucide-react';

export const SecurityDashboard = () => {
    const [stats, setStats] = useState(null);
    const [topIps, setTopIps] = useState([]);
    const [topEndpoints, setTopEndpoints] = useState([]);
    const [blockedIps, setBlockedIps] = useState([]);
    const [loading, setLoading] = useState(true);
    const [ipToBlock, setIpToBlock] = useState('');
    const [blockReason, setBlockReason] = useState('');
    const [actionLoading, setActionLoading] = useState(false);

    const loadData = async () => {
        setLoading(true);
        try {
            const [statsData, ipsData, endpointsData, blockedData] = await Promise.all([
                securityService.getStats(),
                securityService.getTopIps(5),
                securityService.getTopEndpoints(5),
                securityService.getBlockedIps()
            ]);
            setStats(statsData);
            setTopIps(ipsData);
            setTopEndpoints(endpointsData);
            setBlockedIps(blockedData);
        } catch (error) {
            console.error("Failed to load security data", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleBlockIp = async (e) => {
        e.preventDefault();
        if (!ipToBlock) return;
        setActionLoading(true);
        try {
            await securityService.blockIp(ipToBlock, blockReason);
            setIpToBlock('');
            setBlockReason('');
            await loadData();
        } catch (error) {
            alert("Chặn IP thất bại, vui lòng kiểm tra lại định dạng IP.");
        } finally {
            setActionLoading(false);
        }
    };

    const handleUnblockIp = async (ip) => {
        if (!window.confirm(`Bạn có chắc chắn muốn mở khóa cho IP ${ip}?`)) return;
        try {
            await securityService.unblockIp(ip);
            await loadData();
        } catch (error) {
            alert("Không thể mở khóa IP này.");
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-full min-h-[500px]">
                <RefreshCw className="animate-spin text-gray-500" size={32} />
            </div>
        );
    }

    return (
        <div className="p-6 max-w-7xl mx-auto space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                    <Shield className="text-[#100357]" size={28} />
                    Giám Sát Bảo Mật & Phòng Chống Spam
                </h1>
                <button onClick={loadData} className="p-2 border rounded hover:bg-gray-100 flex items-center gap-1 text-sm">
                    <RefreshCw size={16} /> Làm mới
                </button>
            </div>

            {/* Thống kê Tổng quan */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
                    <div>
                        <p className="text-sm font-medium text-gray-500">Lượt Yêu Cầu Hôm Nay</p>
                        <h3 className="text-2xl font-bold text-gray-800 mt-1">{stats?.totalRequestsToday || 0}</h3>
                    </div>
                    <div className="bg-blue-50 p-3 rounded-full text-blue-600">
                        <ShieldCheck size={24} />
                    </div>
                </div>

                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
                    <div>
                        <p className="text-sm font-medium text-gray-500">IP Bị Chặn (Blacklist)</p>
                        <h3 className="text-2xl font-bold text-gray-800 mt-1">{stats?.totalBlockedIps || 0}</h3>
                    </div>
                    <div className="bg-red-50 p-3 rounded-full text-red-600">
                        <ShieldAlert size={24} />
                    </div>
                </div>

                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
                    <div>
                        <p className="text-sm font-medium text-gray-500">Tần Suất Request TB</p>
                        <h3 className="text-2xl font-bold text-gray-800 mt-1">{stats?.avgRequestPerMinute || 0}/phút</h3>
                    </div>
                    <div className="bg-green-50 p-3 rounded-full text-green-600">
                        <RefreshCw size={24} />
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Top IPs hoạt động nhiều */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">Các IP Gửi Nhiều Request Nhất (Hôm nay)</h2>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm border-collapse">
                            <thead>
                            <tr className="bg-gray-50">
                                <th className="p-3 font-semibold text-gray-600">Địa chỉ IP</th>
                                <th className="p-3 font-semibold text-gray-600">Lượt gửi</th>
                                <th className="p-3 font-semibold text-gray-600">Hành động</th>
                            </tr>
                            </thead>
                            <tbody>
                            {topIps.map((ip, index) => (
                                <tr key={index} className="border-t hover:bg-gray-50">
                                    <td className="p-3 font-medium text-gray-700">{ip.ipAddress}</td>
                                    <td className="p-3 text-gray-600">{ip.requestCount}</td>
                                    <td className="p-3">
                                        <button
                                            onClick={() => { setIpToBlock(ip.ipAddress); setBlockReason('Phát hiện spam request lớn'); }}
                                            className="text-red-600 hover:text-red-800 font-medium text-xs flex items-center gap-1"
                                        >
                                            <Ban size={12} /> Chặn nhanh
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Top Endpoints được gọi nhiều */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">Các API Bị Truy Cập Nhiều Nhất</h2>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm border-collapse">
                            <thead>
                            <tr className="bg-gray-50">
                                <th className="p-3 font-semibold text-gray-600">API Endpoint</th>
                                <th className="p-3 font-semibold text-gray-600">Phương thức</th>
                                <th className="p-3 font-semibold text-gray-600">Số lượt gọi</th>
                            </tr>
                            </thead>
                            <tbody>
                            {topEndpoints.map((ep, index) => (
                                <tr key={index} className="border-t hover:bg-gray-50">
                                    <td className="p-3 font-mono text-xs text-gray-700">{ep.uri}</td>
                                    <td className="p-3">
                      <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                          ep.method === 'POST' ? 'bg-orange-100 text-orange-800' :
                              ep.method === 'DELETE' ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'
                      }`}>
                        {ep.method}
                      </span>
                                    </td>
                                    <td className="p-3 text-gray-600">{ep.requestCount}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Form chặn IP mới */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 lg:col-span-1">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">Thêm IP vào Blacklist</h2>
                    <form onSubmit={handleBlockIp} className="space-y-4">
                        <div>
                            <label className="block text-xs font-semibold text-gray-500 mb-1">ĐỊA CHỈ IP</label>
                            <input
                                type="text"
                                placeholder="Ví dụ: 192.168.1.50"
                                value={ipToBlock}
                                onChange={(e) => setIpToBlock(e.target.value)}
                                required
                                className="w-full px-3 py-2 border rounded focus:outline-none focus:border-[#100357]"
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-semibold text-gray-500 mb-1">LÝ DO CHẶN</label>
                            <textarea
                                placeholder="Nhập lý do chặn truy cập..."
                                value={blockReason}
                                onChange={(e) => setBlockReason(e.target.value)}
                                rows={3}
                                className="w-full px-3 py-2 border rounded focus:outline-none focus:border-[#100357]"
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={actionLoading}
                            className="w-full py-2 bg-red-600 hover:bg-red-700 text-white rounded font-medium text-sm flex justify-center items-center gap-1 disabled:opacity-50"
                        >
                            <Ban size={16} /> Chặn Truy Cập
                        </button>
                    </form>
                </div>

                {/* Danh sách IP đang bị chặn */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 lg:col-span-2">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">Danh Sách Địa Chỉ IP Đang Bị Chặn</h2>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm border-collapse">
                            <thead>
                            <tr className="bg-gray-50">
                                <th className="p-3 font-semibold text-gray-600">IP Address</th>
                                <th className="p-3 font-semibold text-gray-600">Lý do</th>
                                <th className="p-3 font-semibold text-gray-600">Người chặn</th>
                                <th className="p-3 font-semibold text-gray-600">Hành động</th>
                            </tr>
                            </thead>
                            <tbody>
                            {blockedIps.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="p-4 text-center text-gray-400">Không có IP nào bị chặn trong hệ thống.</td>
                                </tr>
                            ) : (
                                blockedIps.map((bIp) => (
                                    <tr key={bIp.ipAddress} className="border-t hover:bg-gray-50">
                                        <td className="p-3 font-mono font-medium text-red-600">{bIp.ipAddress}</td>
                                        <td className="p-3 text-gray-600">{bIp.reason || 'Không rõ lý do'}</td>
                                        <td className="p-3 text-gray-500 text-xs">{bIp.createdBy}</td>
                                        <td className="p-3">
                                            <button
                                                onClick={() => handleUnblockIp(bIp.ipAddress)}
                                                className="text-gray-500 hover:text-green-600 flex items-center gap-1 text-xs"
                                            >
                                                <Trash2 size={14} /> Mở khóa
                                            </button>
                                        </td>
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