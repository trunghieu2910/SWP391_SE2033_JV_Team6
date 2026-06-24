import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import medicalRecordService from '../../services/medicalRecordService'
import StatusBadge from '../../components/StatusBadge'
import { Calendar, ArrowRight } from 'lucide-react'

/* ---- Helpers ---- */
function calcAge(dob) {
  if (!dob) return '—'
  const diff = Date.now() - new Date(dob).getTime()
  return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25)) + ' tuổi'
}
function fmtDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
function fmtDateTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN')
}
function initials(name) {
  if (!name) return '?'
  const parts = name.trim().split(' ')
  return (parts[0][0] + (parts[parts.length - 1][0] || '')).toUpperCase()
}
function bmi(w, h) {
  if (!w || !h) return '—'
  const val = w / (h / 100) ** 2
  return val.toFixed(1)
}

/* ---- Section wrapper ---- */
function Section({ icon, title, sub, children, review }) {
  return (
      <div className={review ? 'review-card' : 'det-section'}>
        <div className="sec-header">
          <div className="sec-icon">{icon}</div>
          <span className="sec-title">{title}</span>
          {sub && <span className="sec-sub">{sub}</span>}
        </div>
        {children}
      </div>
  )
}

export default function MedicalRecordDetailPage() {
  const { id } = useParams()
  const navigate       = useNavigate()
  const isDoctorPath = window.location.pathname.startsWith('/doctor')
  const backUrl = isDoctorPath ? '/doctor/medical-records' : '/medical-records'

  const [record,  setRecord]  = useState(null)
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    medicalRecordService.getRecordDetail(id)
        .then(res => { if (active) setRecord(res.data) })
        .catch(err => {
          if (active) setError(err?.response?.data?.message || 'Không tìm thấy bệnh án.')
        })
        .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id])

  if (loading) {
    return (
        <main className="page z1">
          <div className="container">
            <div className="empty" style={{ gridColumn: 'unset' }}>
              <div className="empty-icon">⏳</div>
              <h3>Đang tải bệnh án...</h3>
            </div>
          </div>
        </main>
    )
  }

  if (error || !record) {
    return (
        <main className="page z1">
          <div className="container">
            <div className="empty" style={{ gridColumn: 'unset' }}>
              <div className="empty-icon">❌</div>
              <h3>Không tìm thấy bệnh án</h3>
              <p>{error || `Phiên khám #${id} không tồn tại trong hệ thống.`}</p>
              <button className="back-btn" style={{ margin: '20px auto 0' }} onClick={() => navigate(backUrl)}>
                ← Quay lại
              </button>
            </div>
          </div>
        </main>
    )
  }

  const {
    sessionID, createdAt, status, weight, height, isShared,
    patientFullName, patientNationalID, patientGender, patientDob, patientAddress, patientPhone,
    doctorFullName,
    symptomResultID, symptomResultStatus, symptoms,
    menopauseStatus, symptomDuration, symptomProgressing,
    labTests, medicalImages,
    reviewID, finalDiagnosis, treatmentPlan, doctorAdvice, note, reviewedAt, reviewedByDoctorName,
  } = record

  const hasReview = !!reviewID

  const isFemale = patientGender === 'Female'
  const name     = patientFullName || '—'

  return (
    <main className="p-6 max-w-7xl mx-auto space-y-6 text-left animate-fade-in bg-[#f8fafc] min-h-screen">
      {/* Back Button */}
      <div className="flex justify-between items-center pb-2">
        <button 
          className="flex items-center gap-2 px-4 py-2 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-bold text-gray-600 transition-all shadow-sm"
          onClick={() => navigate(backUrl)}
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
          Quay lại danh sách
        </button>
      </div>

      {/* ── PATIENT BANNER ── */}
      <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6 transition-all hover:shadow-md">
        <div className="flex flex-col sm:flex-row items-center gap-5">
          <div className="w-16 h-16 rounded-full bg-blue-900 flex items-center justify-center font-bold text-xl text-white shrink-0 shadow-lg shadow-blue-900/30">
            {initials(name)}
          </div>
          <div className="space-y-3 text-center sm:text-left">
            <h2 className="text-xl font-bold text-gray-900">{name}</h2>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-y-3 gap-x-6">
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">Số CCCD</span>
                <span className="text-xs font-semibold text-gray-700">{patientNationalID ?? '—'}</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">Giới tính</span>
                <span className="text-xs font-semibold text-gray-700">{patientGender === 'Male' ? '♂ Nam' : '♀ Nữ'}</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">Ngày sinh</span>
                <span className="text-xs font-semibold text-gray-700">{fmtDate(patientDob)} ({calcAge(patientDob)})</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">Địa chỉ</span>
                <span className="text-xs font-semibold text-gray-700">{patientAddress ?? '—'}</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">Số điện thoại</span>
                <span className="text-xs font-semibold text-gray-700">{patientPhone ?? '—'}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-row md:flex-col items-center md:items-end gap-3 self-center shrink-0">
          <span className="bg-[#e6f4ea] text-[#137333] border border-[#ceead6] px-3.5 py-1.5 rounded-full text-xs font-bold inline-flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-[#137333]"></span>
            {status === 'COMPLETED' ? 'HOÀN THÀNH' : status || 'ĐANG XỬ LÝ'}
          </span>
          <span className="bg-gray-50 border border-gray-100 text-gray-600 px-3.5 py-1.5 rounded-full text-xs font-bold inline-flex items-center gap-1.5">
            🔒 {isShared ? 'Đã chia sẻ' : 'Không chia sẻ'}
          </span>
        </div>
      </div>

      {/* ── 1. DIAGNOSIS SESSION ── */}
      <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm transition-all hover:shadow-md space-y-6">
        <div className="flex items-center justify-between border-b border-gray-50 pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 rounded-xl text-blue-600">
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M4.5 16.5c-1.5 1.25-2.5 3-2.5 4.5h20c0-1.5-1-3.25-2.5-4.5" />
                <path d="M12 15V3m0 0l-3 3m3-3l3 3" />
                <circle cx="12" cy="15" r="3" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-gray-800">Thông Tin Phiên Khám</h3>
          </div>
          <span className="font-mono text-sm font-bold text-gray-400">#S{String(sessionID).padStart(4, '0')}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-y-5 gap-x-6 text-xs">
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Mã phiên khám</span>
            <span className="text-sm font-mono font-bold text-gray-800">#S{String(sessionID).padStart(4, '0')}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Bác sĩ phụ trách</span>
            <span className="text-sm font-semibold text-gray-700">{doctorFullName ?? '—'}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Trạng thái</span>
            <span className="bg-[#e6f4ea] text-[#137333] border border-[#ceead6] px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-[#137333]"></span>
              {status === 'COMPLETED' ? 'HOÀN THÀNH' : status || 'ĐANG XỬ LÝ'}
            </span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Cân nặng</span>
            <span className="text-sm font-semibold text-gray-700">{weight ? `${weight} kg` : '—'}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Chiều cao</span>
            <span className="text-sm font-semibold text-gray-700">{height ? `${height} cm` : '—'}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Chỉ số BMI</span>
            <span className="text-sm font-bold text-blue-600">{bmi(weight, height)}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Ngày tạo phiên khám</span>
            <span className="text-sm font-semibold text-gray-700">{fmtDateTime(createdAt)}</span>
          </div>
          <div>
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Chia sẻ hồ sơ</span>
            <span className="bg-gray-100 border border-gray-200 text-gray-700 px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1">
              🔒 {isShared ? 'Đã chia sẻ' : 'Riêng tư'}
            </span>
          </div>
        </div>
      </div>

      {/* ── 2. SYMPTOM RESULT ── */}
      {symptomResultID && (
        <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm transition-all hover:shadow-md space-y-6">
          <div className="flex items-center justify-between border-b border-gray-50 pb-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-blue-50 rounded-xl text-blue-600">
                <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
              </div>
              <h3 className="text-base font-bold text-gray-800">Kết Quả Triệu Chứng</h3>
            </div>
            <span className="text-xs font-bold text-gray-400">Trạng thái: {symptomResultStatus ?? '—'}</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-y-5 gap-x-6 text-xs">
            <div>
              <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Mã kết quả triệu chứng</span>
              <span className="text-sm font-mono font-bold text-gray-800">#SR{symptomResultID}</span>
            </div>
            <div>
              <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Tình trạng mãn kinh</span>
              <span className="text-sm font-semibold text-gray-700">{menopauseStatus ?? '—'}</span>
            </div>
            <div>
              <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Thời gian triệu chứng</span>
              <span className="text-sm font-semibold text-gray-700">{symptomDuration ?? '—'}</span>
            </div>
            <div>
              <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Triệu chứng nặng dần</span>
              <span className="bg-gray-100 border border-gray-200 text-gray-700 px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1">
                {symptomProgressing ? '⚠️ Có – Đang nặng dần' : '✓ Không – Ổn định'}
              </span>
            </div>
            <div>
              <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Trạng thái</span>
              <span className="bg-[#e6f4ea] text-[#137333] border border-[#ceead6] px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-[#137333]"></span>
                HOÀN THÀNH
              </span>
            </div>
          </div>

          {/* ── Symptom List ── */}
          <div className="border-t border-gray-50 pt-5 space-y-3">
            <div className="flex items-center gap-2 text-xs font-bold text-gray-800">
              <span>📌</span>
              <span>Danh sách triệu chứng đã chọn ({symptoms?.length ?? 0})</span>
            </div>
            {symptoms && symptoms.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {symptoms.map(s => (
                  <span 
                    key={s.symptomID} 
                    className="bg-blue-50/70 border border-blue-100 text-blue-800 px-4 py-1.5 rounded-full text-xs font-medium"
                  >
                    {s.symptomName}
                  </span>
                ))}
              </div>
            ) : (
              <p className="text-xs text-gray-400">Không có triệu chứng nào được ghi nhận.</p>
            )}
          </div>
        </div>
      )}

      {/* ── 3. LAB RESULTS ── */}
      <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm transition-all hover:shadow-md space-y-6">
        <div className="flex items-center justify-between border-b border-gray-50 pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 rounded-xl text-blue-600">
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M10 2v7.5M14 2v7.5M8.5 2h7M14 9.5a4.5 4.5 0 11-4 0M10 9.5h4" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-gray-800">Kết Quả Xét Nghiệm</h3>
          </div>
          <span className="text-xs font-bold text-gray-400">{labTests?.length ?? 0} chỉ định</span>
        </div>

        {!labTests || labTests.length === 0 ? (
          <p className="text-xs text-gray-400">Chưa có chỉ định xét nghiệm nào.</p>
        ) : (
          labTests.map((lr, i) => (
            <div key={lr.labResultID} className={i > 0 ? 'border-t border-gray-100 pt-6 space-y-4' : 'space-y-4'}>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-y-4 gap-x-6 text-xs">
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Mã xét nghiệm</span>
                  <span className="text-sm font-mono font-bold text-gray-800">#LR{lr.labResultID}</span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Loại xét nghiệm</span>
                  <span className="text-sm font-semibold text-gray-700">{lr.testType}</span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Trạng thái</span>
                  <span className="bg-[#e6f4ea] text-[#137333] border border-[#ceead6] px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-[#137333]"></span>
                    HOÀN THÀNH
                  </span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Ngày chỉ định</span>
                  <span className="text-sm font-semibold text-gray-700">{fmtDateTime(lr.createdAt)}</span>
                </div>
              </div>

              {/* Parameters Table */}
              {lr.parameters && lr.parameters.length > 0 && (
                <div className="space-y-2">
                  <div className="text-xs font-bold text-gray-500">
                    📊 Thông số xét nghiệm ({lr.parameters.length})
                  </div>
                  <div className="overflow-x-auto rounded-xl border border-gray-100">
                    <table className="min-w-full divide-y divide-gray-100 text-xs">
                      <thead className="bg-gray-50 text-gray-500 font-bold uppercase tracking-wider">
                        <tr>
                          <th className="px-4 py-3 text-left">#</th>
                          <th className="px-4 py-3 text-left">Tên thông số</th>
                          <th className="px-4 py-3 text-left">Đơn vị</th>
                          <th className="px-4 py-3 text-left">Giá trị</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-100 text-gray-700">
                        {lr.parameters.map((lp, pi) => (
                          <tr key={lp.labResultParameterID ?? pi} className="hover:bg-gray-50/50">
                            <td className="px-4 py-3 text-gray-400 font-semibold">{pi + 1}</td>
                            <td className="px-4 py-3 font-semibold">{lp.paramName ?? '—'}</td>
                            <td className="px-4 py-3 text-gray-400 font-medium">{lp.unit ?? '—'}</td>
                            <td className="px-4 py-3 font-bold text-blue-900">{lp.value}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* ── 4. MEDICAL IMAGES ── */}
      <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm transition-all hover:shadow-md space-y-6">
        <div className="flex items-center justify-between border-b border-gray-50 pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 rounded-xl text-blue-600">
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-gray-800">Hình Ảnh Y Khoa</h3>
          </div>
          <span className="text-xs font-bold text-gray-400">{medicalImages?.length ?? 0} chỉ định</span>
        </div>

        {!medicalImages || medicalImages.length === 0 ? (
          <p className="text-xs text-gray-400">Chưa có chỉ định hình ảnh nào.</p>
        ) : (
          medicalImages.map((mi, i) => (
            <div key={mi.medicalImageID} className={i > 0 ? 'border-t border-gray-100 pt-6 space-y-4' : 'space-y-4'}>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-y-4 gap-x-6 text-xs">
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Mã hình ảnh</span>
                  <span className="text-sm font-mono font-bold text-gray-800">#MI{mi.medicalImageID}</span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Loại hình ảnh</span>
                  <span className="text-sm font-semibold text-gray-700">{mi.imageType}</span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Trạng thái</span>
                  <span className="bg-[#e6f4ea] text-[#137333] border border-[#ceead6] px-3 py-1 rounded-full text-[11px] font-bold inline-flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-[#137333]"></span>
                    HOÀN THÀNH
                  </span>
                </div>
                <div>
                  <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Ngày chỉ định</span>
                  <span className="text-sm font-semibold text-gray-700">{fmtDateTime(mi.createdAt)}</span>
                </div>
              </div>

              {mi.details && mi.details.length > 0 && (
                <div className="space-y-2">
                  <div className="text-xs font-bold text-gray-500">🖼 Danh sách ảnh ({mi.details.length})</div>
                  <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-4">
                    {mi.details.map(d => (
                      <div key={d.imageID} className="bg-gray-50 border border-gray-100 rounded-2xl p-3 text-center flex flex-col items-center gap-2">
                        <div className="w-24 h-24 bg-gray-200 rounded-xl overflow-hidden flex items-center justify-center relative shadow-sm border border-gray-100">
                          <img
                            className="object-cover w-full h-full"
                            src={d.imageUrl}
                            alt={mi.imageType}
                            onError={e => {
                              e.target.style.display = 'none'
                              e.target.nextSibling.style.display = 'flex'
                            }}
                          />
                          <div className="hidden absolute inset-0 items-center justify-center text-gray-400 text-3xl bg-gray-100">🖼️</div>
                        </div>
                        <span className="text-[10px] text-gray-400 truncate max-w-[120px] font-mono" title={d.imageUrl}>
                          {d.imageUrl.split('/').pop() || d.imageUrl}
                        </span>
                        <span className="text-[9px] text-gray-400 font-medium">
                          Upload: {fmtDateTime(d.uploadedAt)}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* ── 5. REVIEW / KẾT LUẬN BÁC SĨ ── */}
      <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm transition-all hover:shadow-md space-y-6">
        <div className="flex items-center justify-between border-b border-gray-50 pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 rounded-xl text-blue-600">
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-gray-800">Kết Luận Bác Sĩ</h3>
          </div>
        </div>

        {hasReview ? (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-y-4 gap-x-6 text-xs">
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Mã đánh giá</span>
                <span className="text-sm font-mono font-bold text-gray-800">#R{reviewID}</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Bác sĩ kết luận</span>
                <span className="text-sm font-semibold text-gray-700">{reviewedByDoctorName ?? doctorFullName ?? '—'}</span>
              </div>
              <div>
                <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">Ngày kết luận</span>
                <span className="text-sm font-semibold text-gray-700">{fmtDateTime(reviewedAt)}</span>
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">🔬 Chẩn đoán cuối cùng</span>
                <div className="border-l-4 border-blue-900 bg-gray-100 p-4 rounded-r-xl text-sm font-bold text-gray-800 shadow-sm">
                  {finalDiagnosis}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">💊 Phác đồ điều trị</span>
                  <div className="border-l-4 border-blue-900 bg-gray-100 p-4 rounded-r-xl text-sm font-semibold text-gray-700 shadow-sm">
                    {treatmentPlan}
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">💡 Lời khuyên bác sĩ</span>
                  <div className="border-l-4 border-blue-900 bg-gray-100 p-4 rounded-r-xl text-sm font-semibold text-gray-700 shadow-sm">
                    {doctorAdvice}
                  </div>
                </div>
              </div>

              {note && (
                <div className="flex flex-col gap-1.5">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">📎 Ghi chú thêm</span>
                  <div className="border-l-4 border-blue-900 bg-gray-100 p-4 rounded-r-xl text-sm font-semibold text-gray-700 shadow-sm">
                    {note}
                  </div>
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="bg-gray-50 border border-dashed border-gray-200 rounded-2xl p-8 text-center flex flex-col items-center justify-center gap-2">
            <span className="text-3xl">⏳</span>
            <div className="text-sm font-bold text-gray-700">Chưa có kết luận từ bác sĩ cho phiên khám này.</div>
            <div className="text-xs text-gray-400">Kết quả sẽ được cập nhật sau khi bác sĩ hoàn tất đánh giá.</div>
          </div>
        )}
      </div>
    </main>
  )
}
