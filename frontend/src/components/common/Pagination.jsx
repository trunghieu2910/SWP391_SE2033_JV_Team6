import React from 'react';
import { FaChevronLeft, FaChevronRight } from 'react-icons/fa';

const Pagination = ({ page, totalPages, onPageChange }) => {
    const getPageNumbers = () => {
        const pages = [];
        for (let i = 0; i < totalPages; i++) {
            if (i === 0 || i === totalPages - 1 || (i >= page - 1 && i <= page + 1)) {
                pages.push(i);
            } else if (pages[pages.length - 1] !== '...') {
                pages.push('...');
            }
        }
        return pages;
    };

    if (totalPages <= 1) return null;

    return (
        <div className="flex items-center justify-between gap-4">
            <button
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
                className="px-3 py-1 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
                <FaChevronLeft className="w-4 h-4" />
            </button>

            <div className="flex items-center gap-1">
                {getPageNumbers().map((p, idx) => (
                    <button
                        key={idx}
                        onClick={() => typeof p === 'number' && onPageChange(p)}
                        className={`px-3 py-1 rounded-lg ${
                            p === page
                                ? 'bg-[#100357] text-white'
                                : p === '...'
                                    ? 'cursor-default'
                                    : 'border border-gray-300 hover:bg-gray-50'
                        }`}
                        disabled={p === '...'}
                    >
                        {p === '...' ? '...' : p + 1}
                    </button>
                ))}
            </div>

            <button
                onClick={() => onPageChange(page + 1)}
                disabled={page === totalPages - 1}
                className="px-3 py-1 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
                <FaChevronRight className="w-4 h-4" />
            </button>
        </div>
    );
};

export default Pagination;