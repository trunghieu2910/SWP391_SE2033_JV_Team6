import React, { useState } from 'react';
import { FaChevronDown, FaChevronUp, FaFlask, FaPlus } from 'react-icons/fa';
import toast from 'react-hot-toast';

const LabResults = ({ labResults, sessionId }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [showAddForm, setShowAddForm] = useState(false);

    const handleAddLabResult = () => {
        // Hiện tại chỉ hiển thị thông báo vì backend chưa có API
        toast.info('Tính năng đang phát triển. Vui lòng quay lại sau.');
        setShowAddForm(false);
    };

    return (
        <div className="border border-gray-200 rounded-lg overflow-hidden">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 transition"
            >
                <div className="flex items-center gap-2">
                    <FaFlask className="text-[#100357]" />
                    <span className="font-semibold text-gray-800">Xét nghiệm y tế</span>
                    <span className="text-xs bg-gray-200 px-2 py-0.5 rounded-full">
            {labResults?.length || 0}
          </span>
                </div>
                {isOpen ? <FaChevronUp /> : <FaChevronDown />}
            </button>

            {isOpen && (
                <div className="p-4 border-t border-gray-200">
                    {labResults && labResults.length > 0 ? (
                        <div className="space-y-4">
                            {labResults.map((lab, idx) => (
                                <div key={idx} className="border border-gray-100 rounded-lg p-3">
                                    <div className="flex justify-between items-start mb-2">
                                        <div>
                                            <p className="font-medium text-gray-800">{lab.testType}</p>
                                            <p className="text-xs text-gray-400">
                                                {new Date(lab.createdAt).toLocaleString()}
                                            </p>
                                        </div>
                                        <span className={`px-2 py-0.5 rounded-full text-xs ${
                                            lab.status === 'COMPLETED'
                                                ? 'bg-green-100 text-green-700'
                                                : 'bg-yellow-100 text-yellow-700'
                                        }`}>
                      {lab.status === 'COMPLETED' ? 'Đã có kết quả' : 'Đang xử lý'}
                    </span>
                                    </div>

                                    {lab.parameters && lab.parameters.length > 0 ? (
                                        <div className="mt-2 space-y-1">
                                            {lab.parameters.map((param, pIdx) => (
                                                <div key={pIdx} className="flex justify-between text-sm">
                                                    <span className="text-gray-600">{param.parameterName}</span>
                                                    <span className="font-medium">
                            {param.value} {param.unit}
                          </span>
                                                </div>
                                            ))}
                                        </div>
                                    ) : lab.status === 'PENDING' ? (
                                        <p className="text-gray-400 text-sm mt-2">Chưa có kết quả chi tiết</p>
                                    ) : null}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-gray-400 text-center py-4">Chưa có xét nghiệm nào</p>
                    )}

                    <button
                        onClick={() => setShowAddForm(!showAddForm)}
                        className="mt-4 w-full py-2 border border-dashed border-gray-300 rounded-lg text-gray-500 hover:text-[#100357] hover:border-[#100357] transition flex items-center justify-center gap-2"
                    >
                        <FaPlus className="w-3 h-3" />
                        Thêm xét nghiệm
                    </button>

                    {showAddForm && (
                        <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-600 mb-2">Tính năng đang được phát triển</p>
                            <button
                                onClick={handleAddLabResult}
                                className="px-3 py-1 bg-[#100357] text-white rounded-lg text-sm hover:bg-[#100357]/90"
                            >
                                Xác nhận (Demo)
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default LabResults;