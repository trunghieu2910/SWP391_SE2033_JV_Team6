import React from 'react';
import { FaUserCircle, FaCalendarAlt, FaVenusMars, FaMapMarkerAlt, FaWeight, FaRulerVertical } from 'react-icons/fa';

const PatientInfoCard = ({ session }) => {
    const formatDate = (date) => {
        if (!date) return '—';
        return new Date(date).toLocaleDateString('vi-VN');
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-4 border border-gray-100">
            <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                <FaUserCircle className="text-[#100357]" />
                Hồ sơ bệnh nhân
            </h3>

            <div className="space-y-3">
                <div>
                    <p className="text-xs text-gray-400 uppercase">Mã ca chẩn đoán</p>
                    <p className="font-medium text-gray-800">#{session?.sessionId}</p>
                </div>

                <div>
                    <p className="text-xs text-gray-400 uppercase">Mã bệnh nhân</p>
                    <p className="font-medium text-gray-800">#{session?.patientId}</p>
                </div>

                <div>
                    <p className="text-xs text-gray-400 uppercase">Tên bệnh nhân</p>
                    <p className="font-medium text-gray-800">{session?.patientFullName || '—'}</p>
                </div>

                <div className="flex items-center gap-2">
                    <FaCalendarAlt className="text-gray-400 text-xs" />
                    <div>
                        <p className="text-xs text-gray-400 uppercase">Ngày sinh</p>
                        <p className="text-sm text-gray-800">{formatDate(session?.patientDob)}</p>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    <FaVenusMars className="text-gray-400 text-xs" />
                    <div>
                        <p className="text-xs text-gray-400 uppercase">Giới tính</p>
                        <p className="text-sm text-gray-800">{session?.patientGender === 'Male' ? 'Nam' : 'Nữ'}</p>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    <FaMapMarkerAlt className="text-gray-400 text-xs" />
                    <div>
                        <p className="text-xs text-gray-400 uppercase">Địa chỉ</p>
                        <p className="text-sm text-gray-800">{session?.patientAddress || '—'}</p>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-2 pt-2 border-t border-gray-100">
                    <div>
                        <p className="text-xs text-gray-400 uppercase">Chiều cao</p>
                        <p className="font-medium text-gray-800">{session?.height ? `${session.height} cm` : '—'}</p>
                    </div>
                    <div>
                        <p className="text-xs text-gray-400 uppercase">Cân nặng</p>
                        <p className="font-medium text-gray-800">{session?.weight ? `${session.weight} kg` : '—'}</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PatientInfoCard;