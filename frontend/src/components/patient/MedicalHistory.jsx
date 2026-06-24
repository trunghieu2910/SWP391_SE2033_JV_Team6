import React from 'react';
import { FileText, CheckCircle2, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function MedicalHistory({ recentRecords = [] }) {
  const navigate = useNavigate();

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col transition-all duration-200">
      {/* Title Header */}
      <div className="flex items-center gap-2 mb-5">
        <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
          <FileText className="w-5 h-5" />
        </div>
        <h2 className="text-lg font-bold text-gray-800 tracking-tight">
          Bệnh án gần đây
        </h2>
      </div>

      {recentRecords.length > 0 ? (
        <div className="space-y-4">
          {recentRecords.slice(0, 5).map((record) => {
            const diagnosis = record.diagnosis || 'Viêm phế quản cấp tính';
            const symptoms = record.symptoms || 'Kinh nguyệt kéo dài bất thường, Ra máu sau mãn kinh, Đau vùng chậu, Mệt mỏi kéo dài';
            const visitDate = record.visitDate || '2026-06-20 13:33:04';
            const recordId = record.id || '1';

            return (
              <div
                key={recordId}
                className="group relative border border-gray-100 rounded-2xl p-5 bg-white hover:border-blue-100 hover:shadow-md hover:shadow-blue-50/30 transition-all duration-200 flex flex-col md:flex-row md:items-center justify-between gap-4 cursor-pointer"
                onClick={() => navigate(`/patient/medical-record/${recordId}`)}
              >
                {/* Details */}
                <div className="flex-1 space-y-1">
                  <div className="text-[10px] md:text-xs font-bold text-gray-400 uppercase tracking-wider">
                    PHIÊN KHÁM #{recordId} • {visitDate}
                  </div>
                  <h3 className="text-base font-bold text-gray-800 group-hover:text-blue-600 transition-colors duration-150">
                    {diagnosis}
                  </h3>
                  <p className="text-xs text-gray-500 font-medium leading-relaxed max-w-[580px]">
                    <span className="font-semibold text-gray-400">Triệu chứng:</span> {symptoms}
                  </p>
                </div>

                {/* Badge & Arrow */}
                <div className="flex items-center gap-3 self-start md:self-center shrink-0">
                  <span className="inline-flex items-center gap-1.5 text-xs font-bold text-green-600 bg-green-50 px-3 py-1.5 rounded-xl">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    ĐÃ CÓ CHẨN ĐOÁN
                  </span>
                  <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors duration-150">
                    <ChevronRight className="w-4 h-4" />
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center p-8 text-center bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
          <FileText className="w-10 h-10 text-gray-300 mb-3" />
          <p className="text-sm text-gray-500 font-medium">
            Chưa ghi nhận bệnh án hoàn thành nào trong quá khứ.
          </p>
        </div>
      )}
    </div>
  );
}
