import React, { useState } from 'react';
import { FaChevronDown, FaChevronUp, FaImage, FaPlus, FaEye } from 'react-icons/fa';
import toast from 'react-hot-toast';

const MedicalImages = ({ medicalImages, sessionId }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [showAddForm, setShowAddForm] = useState(false);

    const handleAddImage = () => {
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
                    <FaImage className="text-[#100357]" />
                    <span className="font-semibold text-gray-800">Hình ảnh y tế</span>
                    <span className="text-xs bg-gray-200 px-2 py-0.5 rounded-full">
            {medicalImages?.length || 0}
          </span>
                </div>
                {isOpen ? <FaChevronUp /> : <FaChevronDown />}
            </button>

            {isOpen && (
                <div className="p-4 border-t border-gray-200">
                    {medicalImages && medicalImages.length > 0 ? (
                        <div className="space-y-4">
                            {medicalImages.map((img, idx) => (
                                <div key={idx} className="border border-gray-100 rounded-lg p-3">
                                    <div className="flex justify-between items-start mb-2">
                                        <div>
                                            <p className="font-medium text-gray-800">{img.imageType}</p>
                                            <p className="text-xs text-gray-400">
                                                {new Date(img.createdAt).toLocaleString()}
                                            </p>
                                        </div>
                                        <span className={`px-2 py-0.5 rounded-full text-xs ${
                                            img.status === 'COMPLETED'
                                                ? 'bg-green-100 text-green-700'
                                                : 'bg-yellow-100 text-yellow-700'
                                        }`}>
                      {img.status === 'COMPLETED' ? 'Đã có hình ảnh' : 'Đang xử lý'}
                    </span>
                                    </div>

                                    {img.images && img.images.length > 0 ? (
                                        <div className="grid grid-cols-3 gap-2 mt-2">
                                            {img.images.map((detail, dIdx) => (
                                                <div key={dIdx} className="relative group">
                                                    <img
                                                        src={detail.imageUrl}
                                                        alt={`Medical ${dIdx + 1}`}
                                                        className="w-full h-20 object-cover rounded-lg"
                                                    />
                                                    <button
                                                        className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition flex items-center justify-center rounded-lg"
                                                        onClick={() => window.open(detail.imageUrl, '_blank')}
                                                    >
                                                        <FaEye className="text-white" />
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    ) : img.status === 'PENDING' ? (
                                        <p className="text-gray-400 text-sm mt-2">Chưa có hình ảnh chi tiết</p>
                                    ) : null}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-gray-400 text-center py-4">Chưa có hình ảnh y tế nào</p>
                    )}

                    <button
                        onClick={() => setShowAddForm(!showAddForm)}
                        className="mt-4 w-full py-2 border border-dashed border-gray-300 rounded-lg text-gray-500 hover:text-[#100357] hover:border-[#100357] transition flex items-center justify-center gap-2"
                    >
                        <FaPlus className="w-3 h-3" />
                        Thêm hình ảnh y tế
                    </button>

                    {showAddForm && (
                        <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-600 mb-2">Tính năng đang được phát triển</p>
                            <button
                                onClick={handleAddImage}
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

export default MedicalImages;