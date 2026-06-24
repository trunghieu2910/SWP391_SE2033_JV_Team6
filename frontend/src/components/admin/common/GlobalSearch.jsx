import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, X, User, FileText, Clock } from 'lucide-react';
import { useDebounce } from '../../../hooks/useDebounce';
import adminService from '../../../services/adminService';
import toast from 'react-hot-toast';

const GlobalSearch = () => {
    const navigate = useNavigate();
    const [keyword, setKeyword] = useState('');
    const [results, setResults] = useState({ users: [], logs: [] });
    const [loading, setLoading] = useState(false);
    const [isOpen, setIsOpen] = useState(false);
    const debouncedKeyword = useDebounce(keyword, 300);
    const wrapperRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (debouncedKeyword.length >= 2) {
            search(debouncedKeyword);
        } else {
            setResults({ users: [], logs: [] });
            setIsOpen(false);
        }
    }, [debouncedKeyword]);

    const search = async (keyword) => {
        setLoading(true);
        try {
            const response = await adminService.searchGlobal(keyword);
            setResults(response.data);
            setIsOpen(true);
        } catch (error) {
            console.error('Search error:', error);
            toast.error('Không thể tìm kiếm');
        } finally {
            setLoading(false);
        }
    };

    const handleUserClick = (userId) => {
        setIsOpen(false);
        setKeyword('');
        navigate(`/admin/users/${userId}`);
    };

    const handleLogClick = () => {
        setIsOpen(false);
        setKeyword('');
        navigate('/admin/logs');
    };

    const handleClear = () => {
        setKeyword('');
        setResults({ users: [], logs: [] });
        setIsOpen(false);
    };

    const getStatusColor = (status) => {
        const colors = {
            ACTIVE: 'bg-green-100 text-green-700',
            INACTIVE: 'bg-gray-100 text-gray-700',
            BANNED: 'bg-red-100 text-red-700',
            PENDING: 'bg-yellow-100 text-yellow-700'
        };
        return colors[status] || 'bg-gray-100 text-gray-700';
    };

    const getStatusLabel = (status) => {
        const labels = {
            ACTIVE: 'Đang hoạt động',
            INACTIVE: 'Không hoạt động',
            BANNED: 'Đã bị khóa',
            PENDING: 'Chờ xác nhận'
        };
        return labels[status] || status;
    };

    const totalResults = results.users.length + results.logs.length;

    return (
        <div ref={wrapperRef} className="relative w-[600px]">
            <div className="relative">
                <Search className="absolute left-3 top-1/2 -transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                    type="text"
                    placeholder="Tìm kiếm người dùng hoặc nhật ký..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    onFocus={() => keyword.length >= 2 && setIsOpen(true)}
                    className="w-full pl-10 pr-10 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent transition text-base"
                />
                {keyword && (
                    <button
                        onClick={handleClear}
                        className="absolute right-3 top-1/2 -transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                        <X className="w-4 h-4" />
                    </button>
                )}
            </div>

            {isOpen && keyword.length >= 2 && (
                <div className="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl shadow-xl border border-gray-200 max-h-[480px] overflow-y-auto z-50">
                    {loading ? (
                        <div className="flex items-center justify-center p-4">
                            <div className="w-6 h-6 border-2 border-[#100357] border-t-transparent rounded-full animate-spin"></div>
                            <span className="ml-2 text-sm text-gray-500">Đang tìm kiếm...</span>
                        </div>
                    ) : totalResults === 0 ? (
                        <div className="p-4 text-center text-gray-400">
                            <p>Không tìm thấy kết quả</p>
                        </div>
                    ) : (
                        <div>
                            {/* Users */}
                            {results.users.length > 0 && (
                                <div className="p-2">
                                    <h4 className="text-xs font-semibold text-gray-400 uppercase px-3 py-1 flex items-center gap-2">
                                        <User className="w-3 h-3" />
                                        Người dùng ({results.users.length})
                                    </h4>
                                    {results.users.map((user) => (
                                        <div
                                            key={user.userId}
                                            onClick={() => handleUserClick(user.userId)}
                                            className="flex items-center gap-3 px-3 py-2 hover:bg-gray-50 rounded-lg cursor-pointer transition"
                                        >
                                            <div className="w-8 h-8 bg-[#100357] rounded-full flex items-center justify-center text-white font-medium text-sm">
                                                {user.fullName?.charAt(0) || 'U'}
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <p className="text-sm font-medium text-gray-800 truncate">{user.fullName}</p>
                                                <p className="text-xs text-gray-500 truncate">{user.email}</p>
                                            </div>
                                            <div className="flex items-center gap-2 flex-shrink-0">
                                                <span className="text-xs bg-gray-100 px-2 py-0.5 rounded-full text-gray-600">
                                                    {user.roleName}
                                                </span>
                                                <span className={`text-xs px-2 py-0.5 rounded-full ${getStatusColor(user.status)}`}>
                                                    {getStatusLabel(user.status)}
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {/* Logs */}
                            {results.logs.length > 0 && (
                                <div className="p-2 border-t border-gray-100">
                                    <h4 className="text-xs font-semibold text-gray-400 uppercase px-3 py-1 flex items-center gap-2">
                                        <FileText className="w-3 h-3" />
                                        Nhật ký ({results.logs.length})
                                    </h4>
                                    {results.logs.map((log) => (
                                        <div
                                            key={log.logId}
                                            onClick={handleLogClick}
                                            className="px-3 py-2 hover:bg-gray-50 rounded-lg cursor-pointer transition"
                                        >
                                            <p className="text-sm text-gray-800 truncate">{log.description}</p>
                                            <div className="flex items-center gap-2 text-xs text-gray-400 mt-1">
                                                <Clock className="w-3 h-3" />
                                                <span>{new Date(log.performedAt).toLocaleString('vi-VN')}</span>
                                                <span>•</span>
                                                <span className="font-medium text-gray-600">{log.username}</span>
                                                <span className="px-2 py-0.5 bg-gray-100 rounded-full text-gray-600">
                                                    {log.action}
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {/* Footer */}
                            <div className="p-2 border-t border-gray-100 text-center">
                                <button
                                    onClick={handleLogClick}
                                    className="text-sm text-[#100357] hover:underline font-medium"
                                >
                                    Xem tất cả kết quả nhật ký →
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default GlobalSearch;