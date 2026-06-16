import React from 'react';

const LogsTable = ({ logs, startIndex = 0 }) => {
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

    return (
        <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white shadow-sm">
            <table className="w-full min-w-[1000px]">
                <thead className="bg-gray-50 rounded-t-xl">
                <tr className="text-left text-gray-600 text-sm">
                    <th className="px-4 py-3 rounded-tl-xl w-16">STT</th>
                    <th className="px-4 py-3 w-20">Mã log</th>
                    <th className="px-4 py-3 w-40">Thời gian</th>
                    <th className="px-4 py-3 w-32">Hành động</th>
                    <th className="px-4 py-3 w-28">Loại đối tượng</th>
                    <th className="px-4 py-3 w-24">ID đối tượng</th>
                    <th className="px-4 py-3 w-80">Mô tả</th>
                    <th className="px-4 py-3 rounded-tr-xl w-28">Người dùng</th>
                </tr>
                </thead>
                <tbody>
                {logs?.length === 0 ? (
                    <tr>
                        <td colSpan="8" className="px-4 py-8 text-center text-gray-400">
                            Không có dữ liệu log
                        </td>
                    </tr>
                ) : (
                    logs?.map((log, index) => (
                        <tr
                            key={log.logId}
                            className={`border-b border-gray-100 hover:bg-gray-50 transition ${
                                index === logs.length - 1 ? 'rounded-b-xl' : ''
                            }`}
                        >
                            <td className="px-4 py-3 text-sm text-gray-500 w-16">{startIndex + index + 1}</td>
                            <td className="px-4 py-3 text-sm font-mono w-20">{log.logId}</td>
                            <td className="px-4 py-3 text-sm w-40 whitespace-nowrap">
                                {new Date(log.performedAt).toLocaleString()}
                            </td>
                            <td className="px-4 py-3 w-32">
                                    <span className={`px-2 py-1 rounded-full text-xs font-medium whitespace-nowrap ${getActionBadgeColor(log.action)}`}>
                                        {getActionLabel(log.action)}
                                    </span>
                            </td>
                            <td className="px-4 py-3 text-sm w-28">{log.targetType}</td>
                            <td className="px-4 py-3 text-sm w-24">{log.targetId}</td>
                            <td className="px-4 py-3 text-sm w-80 break-words">{log.description}</td>
                            <td className="px-4 py-3 text-sm w-28">{log.user?.username || 'Hệ thống'}</td>
                        </tr>
                    ))
                )}
                </tbody>
            </table>
        </div>
    );
};

export default LogsTable;