import { useNavigate } from 'react-router-dom'
import StatusBadge from './StatusBadge.jsx'

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

  return (

    <div className="record-card">
      {/* Header */}
      <div className="card-header">
        <div className={`pat-avatar ${gender ? 'female' : ''}`}>
          {initials(name)}
        </div>
        <div className="card-pat-info">
          <div className="pat-name">{name}</div>
          <div className="pat-meta">
            <span className="info-val">{nationalID || '—'}</span>
            <span className="info-val">
                 {gender === 'Female' ? '♀' : '♂'}{' '}
                 {gender === 'Female' ? 'Nữ' : 'Nam'}
            </span>

          </div>
        </div>
        <div className="card-status">
          <StatusBadge status={status} />
        </div>
      </div>


      {/* Body */}
      <div className="card-body">
        <div className="info-item">
          <span className="info-lbl">Mã phiên khám</span>
          <span className="info-val accent">#S{String(id).padStart(4, '0')}</span>
        </div>
        <div className="info-item">
          <span className="info-lbl">Chẩn đoán</span>
          <span className="info-val">{diagnosis || 'Chưa có chẩn đoán'}</span>
        </div>
        <div className="info-item">
          <span className="info-lbl">Chia sẻ hồ sơ</span>
          <span className={`shared-pill ${isShared ? 'yes' : 'no'}`} style={{ display: 'inline-flex' }}>
            {isShared ? '🔗 Đã chia sẻ' : '🔒 Riêng tư'}
          </span>
        </div>
      </div>

      {/* Symptoms preview */}
      {symptoms && (
        <div className="card-symptoms">
          <span className="sym-tag">{symptoms}</span>
        </div>
      )}

      {/* Footer */}
      <div className="card-footer">
        <span className="session-date">🕐 {fmtDateTime(visitDate)}</span>
        <button
          className="btn-view"
          onClick={() => navigate(`/medical-records/${id}`)}
        >
          Xem chi tiết
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M9 18l6-6-6-6" />
          </svg>
        </button>
      </div>
    </div>
  )
}
