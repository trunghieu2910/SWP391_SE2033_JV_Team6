import React from 'react';
import { FaEye, FaSyncAlt } from 'react-icons/fa';
import StatusBadge from '../../common/StatusBadge';

const UserTable = ({ users, onViewDetail, onUpdateStatus, startIndex = 0 }) => {
    // Hàm lấy màu nền avatar dựa trên vai trò
    const getAvatarColor = (role) => {
        switch (role) {
            case 'ADMIN':
                return 'bg-purple-100 text-purple-600';
            case 'DOCTOR':
                return 'bg-blue-100 text-blue-600';
            case 'PATIENT':
                return 'bg-green-100 text-green-600';
            default:
                return 'bg-gray-100 text-gray-600';
        }
    };

    return (
        <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white shadow-sm">
            <table className="w-full">
                <thead className="bg-gray-50 rounded-t-xl">
                <tr className="text-left text-gray-600 text-sm">
                    <th className="px-4 py-3 rounded-tl-xl">STT</th>
                    <th className="px-4 py-3">Mã người dùng</th>
                    <th className="px-4 py-3">Người dùng</th>
                    <th className="px-4 py-3">Họ và tên</th>
                    <th className="px-4 py-3">Email</th>
                    <th className="px-4 py-3">Vai trò</th>
                    <th className="px-4 py-3">Trạng thái</th>
                    <th className="px-4 py-3 rounded-tr-xl">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                {users?.length === 0 ? (
                    <tr>
                        <td colSpan="8" className="px-4 py-8 text-center text-gray-400">
                            Không tìm thấy người dùng
                        </td>
                    </tr>
                ) : (
                    users?.map((user, index) => (
                        <tr
                            key={user.userId}
                            className={`border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition ${
                                index === users.length - 1 ? 'rounded-b-xl' : ''
                            }`}
                            onClick={() => onViewDetail(user.userId)}
                        >
                            <td className="px-4 py-3 text-sm text-gray-500">{startIndex + index + 1}</td>
                            <td className="px-4 py-3 text-sm">{user.userId}</td>
                            <td className="px-4 py-3">
                                <div className="flex items-center gap-2">
                                    <div className={`w-8 h-8 ${getAvatarColor(user.roleName)} rounded-full flex items-center justify-center font-medium`}>
                                        {user.fullName?.charAt(0) || user.userName?.charAt(0) || 'U'}
                                    </div>
                                    <span className="font-medium text-sm">{user.userName}</span>
                                </div>
                            </td>
                            <td className="px-4 py-3 text-sm">{user.fullName}</td>
                            <td className="px-4 py-3 text-sm truncate max-w-[200px]" title={user.email}>
                                {user.email}
                            </td>
                            <td className="px-4 py-3">
                                <StatusBadge type="role" value={user.roleName} />
                            </td>
                            <td className="px-4 py-3">
                                <StatusBadge type="userStatus" value={user.status} />
                            </td>
                            <td className="px-4 py-3">
                                <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
                                    <button
                                        onClick={() => onViewDetail(user.userId)}
                                        className="p-1 text-gray-500 hover:text-[#100357] transition"
                                        title="Xem chi tiết"
                                    >
                                        <FaEye className="w-4 h-4" />
                                    </button>
                                    {user.roleName !== 'ADMIN' && (
                                        <button
                                            onClick={() => onUpdateStatus(user)}
                                            className="p-1 text-gray-500 hover:text-[#100357] transition"
                                            title="Đổi trạng thái"
                                        >
                                            <FaSyncAlt className="w-4 h-4" />
                                        </button>
                                    )}
                                </div>
                            </td>
                        </tr>
                    ))
                )}
                </tbody>
            </table>
        </div>
    );
};

export default UserTable;