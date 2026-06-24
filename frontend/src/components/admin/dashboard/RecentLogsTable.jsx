import React from 'react';

const RecentLogsTable = ({ logs, onViewAll }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-4">
            <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-gray-800">Nhật ký hoạt động gần đây</h3>
                <button
                    onClick={onViewAll}
                    className="text-[#097300] hover:text-[#097300]/80 text-sm flex items-center gap-1"
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
                    {logs?.length === 0 ? (
                        <tr>
                            <td colSpan="4" className="py-4 text-center text-gray-400">Không có nhật ký hoạt động</td>
                        </tr>
                    ) : (
                        logs?.map((log, idx) => (
                            <tr key={idx} className="border-b border-gray-100 hover:bg-gray-50">
                                <td className="py-2 text-sm">{new Date(log.performedAt).toLocaleString()}</td>
                                <td className="py-2">
                                    <span className="px-2 py-1 bg-gray-100 rounded text-xs">{log.action}</span>
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
    );
};

export default RecentLogsTable;