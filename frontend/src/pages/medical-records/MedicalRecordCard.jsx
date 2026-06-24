import { useNavigate } from 'react-router-dom'
import StatusBadge from '../../components/StatusBadge'
import { Calendar, ArrowRight } from 'lucide-react'

function fmtDateTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN')
}

function initials(name) {
  if (!name) return '?'
  const parts = name.trim().split(' ')
  return (parts[0][0] + (parts[parts.length - 1][0] || '')).toUpperCase()
}

export default function MedicalRecordCard({ record }) {
  const navigate = useNavigate()
  const {
    id,
    patientName,
    diagnosis,
    visitDate,
    symptoms,
    status,
    isShared,
    nationalID,
    gender,
  } = record

  const name = patientName || 'Không rõ'
  const isFemale = gender === 'Female'
  const isDoctorPath = window.location.pathname.startsWith('/doctor')
  const detailUrl = isDoctorPath ? `/doctor/medical-records/${id}` : `/medical-records/${id}`

  return (
    <div 
      className="bg-white rounded-2xl border border-gray-100 p-5 hover:border-blue-200 hover:shadow-lg hover:shadow-blue-50/40 transition-all duration-300 flex flex-col justify-between gap-5 relative cursor-pointer"
      style={{ boxShadow: '0 4px 20px -2px rgba(50, 100, 150, 0.05)' }}
      onClick={() => navigate(detailUrl)}
    >
      {/* Header */}
      <div className="flex items-center justify-between gap-3 border-b border-gray-50 pb-4">
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm text-white shrink-0 ${
            isFemale ? 'bg-pink-500' : 'bg-blue-500'
          }`}>
            {initials(name)}
          </div>
          <div className="min-w-0">
            <div className="text-sm font-bold text-gray-800 truncate">{name}</div>
            <div className="flex items-center gap-2 text-[10px] md:text-xs text-gray-400 mt-0.5">
              <span>{nationalID || '—'}</span>
              <span>•</span>
              <span>{isFemale ? 'Nữ' : 'Nam'}</span>
            </div>
          </div>
        </div>
        <div className="shrink-0">
          <StatusBadge status={status} />
        </div>
      </div>

      {/* Body */}
      <div className="space-y-3 text-xs text-left">
        <div className="flex justify-between items-center">
          <span className="text-gray-400 font-medium">Mã phiên khám</span>
          <span className="font-mono font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
            #S{String(id).padStart(4, '0')}
          </span>
        </div>
        
        <div className="flex flex-col gap-1">
          <span className="text-gray-400 font-medium">Chẩn đoán</span>
          <span className="font-semibold text-gray-700 line-clamp-1">{diagnosis || 'Chưa có chẩn đoán'}</span>
        </div>

        <div className="flex justify-between items-center">
          <span className="text-gray-400 font-medium">Chia sẻ hồ sơ</span>
          <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full font-bold text-[10px] ${
            isShared ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
          }`}>
            {isShared ? '🔗 Đã chia sẻ' : '🔒 Riêng tư'}
          </span>
        </div>
      </div>

      {/* Symptoms preview */}
      {symptoms && (
        <div className="bg-gray-50 rounded-xl p-3 border border-gray-100 text-xs text-left">
          <div className="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Triệu chứng</div>
          <p className="text-gray-600 font-medium line-clamp-2 leading-relaxed">{symptoms}</p>
        </div>
      )}

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-50 text-xs mt-auto">
        <span className="text-gray-400 flex items-center gap-1 font-medium">
          <Calendar className="w-3.5 h-3.5" />
          {fmtDateTime(visitDate)}
        </span>
        <button
          className="text-blue-600 font-bold flex items-center gap-1 hover:text-blue-700 transition-colors"
          onClick={(e) => {
            e.stopPropagation();
            navigate(detailUrl);
          }}
        >
          Chi tiết
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  )
}
