import React from 'react';
import { Clock, CheckCircle, AlertCircle, FileText, X } from 'lucide-react';
import ClinicalForm from '../ClinicalForm';

export default function ActiveSession({
  activeSession,
  showForm,
  setShowForm,
  formSubmitLoading,
  handleFormSubmit
}) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col relative overflow-hidden transition-all duration-200">
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
            <Clock className="w-5 h-5" />
          </div>
          <h2 className="text-lg font-bold text-gray-800 tracking-tight">
            Phiên khám đang hoạt động
          </h2>
        </div>
        {activeSession && (
          <span className="text-[10px] md:text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full uppercase tracking-wider animate-pulse">
            CÓ PHIÊN KHÁM MỚI
          </span>
        )}
      </div>

      {activeSession ? (
        <div className="flex flex-col gap-4">
          {/* Session Details */}
          <div className="grid grid-cols-2 gap-4 bg-gray-50/50 p-4 rounded-xl border border-gray-100">
            <div>
              <span className="text-xs font-semibold text-gray-400 block mb-0.5">Mã phiên khám</span>
              <span className="text-base font-bold text-gray-800">#{activeSession.id}</span>
            </div>
            <div className="text-right">
              <span className="text-xs font-semibold text-gray-400 block mb-0.5">Ngày tạo</span>
              <span className="text-sm font-bold text-gray-800">{activeSession.visitDate || '2026-06-20 15:54:23'}</span>
            </div>
          </div>

          <hr className="border-gray-100" />

          {/* Symptom Submission Status */}
          {activeSession.symptomResult?.status === 'COMPLETED' ? (
            <div className="flex flex-col gap-4">
              <div className="bg-green-50 border border-green-100 rounded-xl p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex gap-3">
                  <div className="p-2 bg-green-100/50 rounded-full text-green-600 shrink-0 w-9 h-9 flex items-center justify-center">
                    <CheckCircle className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-green-800">
                      Bạn đã gửi biểu mẫu triệu chứng thành công
                    </h4>
                    <p className="text-xs text-green-700/80 mt-1 font-medium leading-relaxed">
                      Bác sĩ hiện đã có thể xem đầy đủ thông tin triệu chứng của bạn để đưa ra chẩn đoán.
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setShowForm(!showForm)}
                  className="px-4 py-2 border border-blue-600 text-blue-600 hover:bg-blue-50 text-xs font-bold rounded-xl transition-all duration-200 self-start md:self-center"
                >
                  {showForm ? 'Đóng thông tin' : 'Xem lại thông tin đã gửi'}
                </button>
              </div>

              {showForm && (
                <div className="border border-gray-100 rounded-xl p-4 bg-white mt-2 shadow-inner">
                  <div className="flex items-center justify-between mb-4 pb-2 border-b border-gray-50">
                    <span className="text-sm font-bold text-gray-700">Thông tin triệu chứng đã gửi</span>
                    <button
                      onClick={() => setShowForm(false)}
                      className="p-1 hover:bg-gray-100 rounded-full text-gray-400"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                  <ClinicalForm
                    initialData={{
                      height: activeSession.height,
                      weight: activeSession.weight,
                      menopauseStatus: activeSession.symptomResult.menopauseStatus,
                      symptomDuration: activeSession.symptomResult.symptomDuration,
                      symptomProgressing: activeSession.symptomResult.symptomProgressing,
                      symptomIds: activeSession.symptomResult.symptomIds || []
                    }}
                    readOnly={true}
                  />
                </div>
              )}
            </div>
          ) : (
            // Symptom not submitted yet
            <div className="flex flex-col gap-4">
              {!showForm ? (
                <div className="bg-amber-50 border border-amber-100 rounded-xl p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex gap-3">
                    <div className="p-2 bg-amber-100/50 rounded-full text-amber-600 shrink-0 w-9 h-9 flex items-center justify-center">
                      <AlertCircle className="w-5 h-5" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-amber-800">
                        Chưa hoàn thành khai báo triệu chứng
                      </h4>
                      <p className="text-xs text-amber-700/80 mt-1 font-medium leading-relaxed">
                        Vui lòng hoàn thành khai báo các triệu chứng lâm sàng để bác sĩ có cơ sở chẩn đoán.
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => setShowForm(true)}
                    className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl transition-all duration-200 shadow-sm shadow-blue-150 self-start md:self-center"
                  >
                    Điền thông tin lâm sàng
                  </button>
                </div>
              ) : (
                <div className="border border-gray-150 rounded-xl p-5 bg-white shadow-inner mt-2">
                  <div className="flex items-center justify-between mb-5 pb-3 border-b border-gray-100">
                    <h3 className="text-base font-bold text-gray-800">Khai báo thông tin lâm sàng</h3>
                    <button
                      onClick={() => setShowForm(false)}
                      className="px-3 py-1.5 border border-gray-200 text-gray-500 hover:bg-gray-50 text-xs font-bold rounded-lg transition-all duration-200"
                    >
                      Hủy khai báo
                    </button>
                  </div>
                  <ClinicalForm onSubmit={handleFormSubmit} loading={formSubmitLoading} />
                </div>
              )}
            </div>
          )}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center p-8 text-center bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
          <FileText className="w-10 h-10 text-gray-300 mb-3" />
          <p className="text-sm text-gray-500 font-medium">
            Không có phiên khám nào đang hoạt động.
          </p>
          <p className="text-xs text-gray-400 mt-1 max-w-[320px]">
            Vui lòng liên hệ bác sĩ phụ trách để được tạo phiên khám mới trên hệ thống.
          </p>
        </div>
      )}
    </div>
  );
}
