import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import medicalRecordService from '../../services/medicalRecordService'
import StatusBadge from '../../components/common/StatusBadge.jsx'

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
  const { sessionId } = useParams()
  const navigate       = useNavigate()

  const [record,  setRecord]  = useState(null)
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    medicalRecordService.getRecordDetail(sessionId)
        .then(res => { if (active) setRecord(res.data) })
        .catch(err => {
          if (active) setError(err?.response?.data?.message || 'Không tìm thấy bệnh án.')
        })
        .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId])

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
              <p>{error || `Phiên khám #${sessionId} không tồn tại trong hệ thống.`}</p>
              <button className="back-btn" style={{ margin: '20px auto 0' }} onClick={() => navigate('/medical-records')}>
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

  const isFemale = patientGender === 'Female'
  const name     = patientFullName ?? 'Không rõ'
  const hasReview = !!reviewID

  return (
      <main className="page z1">
        <div className="container">

          {/* Back */}
          <button className="back-btn" onClick={() => navigate('/medical-records')}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
            Quay lại danh sách
          </button>

          {/* ── PATIENT BANNER ── */}
          <div className="patient-banner">
            <div className={`banner-avatar ${isFemale ? 'female' : ''}`}>
              {initials(name)}
            </div>
            <div className="banner-info">
              <h2>{name}</h2>
              <div className="banner-meta">
                <div className="banner-meta-item">
                  <span className="lbl">Số CCCD</span>
                  <span className="val">{patientNationalID ?? '—'}</span>
                </div>
                <div className="banner-meta-item">
                  <span className="lbl">Giới tính</span>
                  <span className="val">{patientGender === 'Male' ? '♂ Nam' : '♀ Nữ'}</span>
                </div>
                <div className="banner-meta-item">
                  <span className="lbl">Ngày sinh</span>
                  <span className="val">{fmtDate(patientDob)} ({calcAge(patientDob)})</span>
                </div>
                <div className="banner-meta-item">
                  <span className="lbl">Địa chỉ</span>
                  <span className="val">{patientAddress ?? '—'}</span>
                </div>
                <div className="banner-meta-item">
                  <span className="lbl">Số điện thoại</span>
                  <span className="val">{patientPhone ?? '—'}</span>
                </div>
              </div>
            </div>
            <div className="banner-badges">
              <StatusBadge status={status} />
              <span className={`shared-pill ${isShared ? 'yes' : 'no'}`}>
              {isShared ? '🔗 Đã chia sẻ' : '🔒 Không chia sẻ'}
            </span>
            </div>
          </div>

          {/* ── 1. DIAGNOSIS SESSION ── */}
          <Section icon="🩺" title="Thông Tin Phiên Khám"
                   sub={`#S${String(sessionID).padStart(4,'0')}`}>
            <div className="detail-grid">
              <div className="det-field">
                <span className="det-lbl">Mã phiên khám</span>
                <span className="det-val accent big">#S{String(sessionID).padStart(4,'0')}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Bác sĩ phụ trách</span>
                <span className="det-val">{doctorFullName ?? '—'}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Trạng thái</span>
                <StatusBadge status={status} />
              </div>
              <div className="det-field">
                <span className="det-lbl">Cân nặng</span>
                <span className="det-val">{weight ? `${weight} kg` : '—'}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Chiều cao</span>
                <span className="det-val">{height ? `${height} cm` : '—'}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Chỉ số BMI</span>
                <span className="det-val accent">{bmi(weight, height)}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Ngày tạo phiên khám</span>
                <span className="det-val">{fmtDateTime(createdAt)}</span>
              </div>
              <div className="det-field">
                <span className="det-lbl">Chia sẻ hồ sơ</span>
                <span className={`shared-pill ${isShared ? 'yes' : 'no'}`} style={{display:'inline-flex'}}>
                {isShared ? '🔗 Đã chia sẻ' : '🔒 Riêng tư'}
              </span>
              </div>
            </div>
          </Section>

          {/* ── 2. SYMPTOM RESULT ── */}
          {symptomResultID && (
              <Section icon="🩻" title="Kết Quả Triệu Chứng"
                       sub={`Trạng thái: ${symptomResultStatus ?? '—'}`}>
                <div className="detail-grid" style={{marginBottom: 16}}>
                  <div className="det-field">
                    <span className="det-lbl">Mã kết quả triệu chứng</span>
                    <span className="det-val accent">#SR{symptomResultID}</span>
                  </div>
                  <div className="det-field">
                    <span className="det-lbl">Tình trạng mãn kinh</span>
                    <span className="det-val">{menopauseStatus ?? '—'}</span>
                  </div>
                  <div className="det-field">
                    <span className="det-lbl">Thời gian triệu chứng</span>
                    <span className="det-val">{symptomDuration ?? '—'}</span>
                  </div>
                  <div className="det-field">
                    <span className="det-lbl">Triệu chứng nặng dần</span>
                    <span className={`radio-pill ${symptomProgressing ? 'yes' : 'no'}`}>
                  {symptomProgressing
                      ? '⚠️ Có – Đang nặng dần'
                      : '✅ Không – Ổn định'}
                </span>
                  </div>
                  <div className="det-field">
                    <span className="det-lbl">Trạng thái</span>
                    <StatusBadge status={symptomResultStatus} />
                  </div>
                </div>

                {/* ── Symptom Details ── */}
                <div className="divider" />
                <div className="sec-header" style={{marginBottom: 12}}>
                  <div className="sec-icon" style={{fontSize: 14}}>📌</div>
                  <span className="sec-title" style={{fontSize: 14}}>
                Danh sách triệu chứng đã chọn ({symptoms?.length ?? 0})
              </span>
                </div>
                {symptoms && symptoms.length > 0
                    ? <div className="sym-chips">
                      {symptoms.map(s => (
                          <span key={s.symptomID} className="sym-chip">{s.symptomName}</span>
                      ))}
                    </div>
                    : <p style={{color:'var(--text-muted)',fontSize:13}}>Không có triệu chứng nào được ghi nhận.</p>
                }
              </Section>
          )}

          {/* ── 3. LAB RESULTS ── */}
          <Section icon="🧪" title="Kết Quả Xét Nghiệm"
                   sub={`${labTests?.length ?? 0} chỉ định`}>
            {!labTests || labTests.length === 0
                ? <p style={{color:'var(--text-muted)',fontSize:13}}>Chưa có chỉ định xét nghiệm nào.</p>
                : labTests.map((lr, i) => (
                    <div key={lr.labResultID} style={i > 0 ? {marginTop: 20} : {}}>
                      <div className="detail-grid-2" style={{marginBottom: 12}}>
                        <div className="det-field">
                          <span className="det-lbl">Mã xét nghiệm</span>
                          <span className="det-val accent">#LR{lr.labResultID}</span>
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Loại xét nghiệm</span>
                          <span className="det-val">{lr.testType}</span>
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Trạng thái</span>
                          <StatusBadge status={lr.status} />
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Ngày chỉ định</span>
                          <span className="det-val">{fmtDateTime(lr.createdAt)}</span>
                        </div>
                      </div>

                      {/* Lab Parameters */}
                      {lr.parameters && lr.parameters.length > 0 && (
                          <>
                            <div className="sec-header" style={{marginBottom: 8}}>
                      <span style={{fontSize:13, color:'var(--text-muted)'}}>
                        📊 Thông số xét nghiệm ({lr.parameters.length})
                      </span>
                            </div>
                            <table className="lab-table">
                              <thead>
                              <tr>
                                <th>#</th>
                                <th>Tên thông số</th>
                                <th>Đơn vị</th>
                                <th>Giá trị</th>
                              </tr>
                              </thead>
                              <tbody>
                              {lr.parameters.map((lp, pi) => (
                                  <tr key={lp.labResultParameterID ?? pi}>
                                    <td style={{color:'var(--text-muted)'}}>{pi + 1}</td>
                                    <td>{lp.paramName ?? '—'}</td>
                                    <td style={{color:'var(--text-muted)'}}>{lp.unit ?? '—'}</td>
                                    <td><span className="lab-val">{lp.value}</span></td>
                                  </tr>
                              ))}
                              </tbody>
                            </table>
                          </>
                      )}
                      {labTests.length > 1 && i < labTests.length - 1 && (
                          <div className="divider" />
                      )}
                    </div>
                ))
            }
          </Section>

          {/* ── 4. MEDICAL IMAGES ── */}
          <Section icon="🖼️" title="Hình Ảnh Y Khoa"
                   sub={`${medicalImages?.length ?? 0} chỉ định`}>
            {!medicalImages || medicalImages.length === 0
                ? <p style={{color:'var(--text-muted)',fontSize:13}}>Chưa có chỉ định hình ảnh nào.</p>
                : medicalImages.map((mi, i) => (
                    <div key={mi.medicalImageID} style={i > 0 ? {marginTop: 20} : {}}>
                      <div className="detail-grid-2" style={{marginBottom: 12}}>
                        <div className="det-field">
                          <span className="det-lbl">Mã hình ảnh</span>
                          <span className="det-val accent">#MI{mi.medicalImageID}</span>
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Loại hình ảnh</span>
                          <span className="det-val">{mi.imageType}</span>
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Trạng thái</span>
                          <StatusBadge status={mi.status} />
                        </div>
                        <div className="det-field">
                          <span className="det-lbl">Ngày chỉ định</span>
                          <span className="det-val">{fmtDateTime(mi.createdAt)}</span>
                        </div>
                      </div>

                      {mi.details && mi.details.length > 0 && (
                          <>
                            <div style={{fontSize:13, color:'var(--text-muted)', marginBottom: 10}}>
                              🖼 Danh sách ảnh ({mi.details.length})
                            </div>
                            <div className="img-gallery">
                              {mi.details.map(d => (
                                  <div key={d.imageID} className="img-card">
                                    <img
                                        src={d.imageUrl}
                                        alt={mi.imageType}
                                        onError={e => {
                                          e.target.style.display = 'none'
                                          e.target.nextSibling.style.display = 'flex'
                                        }}
                                    />
                                    <div style={{
                                      display:'none', width:160, height:100,
                                      alignItems:'center', justifyContent:'center',
                                      background:'rgba(0,0,0,.3)', fontSize:28
                                    }}>🖼️</div>
                                    <span className="img-url" title={d.imageUrl}>{d.imageUrl}</span>
                                    <span style={{
                                      display:'block', padding:'0 10px 6px',
                                      fontSize:10, color:'var(--text-muted)'
                                    }}>
                            Upload: {fmtDateTime(d.uploadedAt)}
                          </span>
                                  </div>
                              ))}
                            </div>
                          </>
                      )}
                      {medicalImages.length > 1 && i < medicalImages.length - 1 && (
                          <div className="divider" />
                      )}
                    </div>
                ))
            }
          </Section>

          {/* ── 5. REVIEW / KẾT LUẬN BÁC SĨ ── */}
          <Section icon="📋" title="Kết Luận Bác Sĩ" review>
            {hasReview ? (
                <>
                  <div className="detail-grid-2" style={{marginBottom: 16}}>
                    <div className="det-field">
                      <span className="det-lbl">Mã đánh giá</span>
                      <span className="det-val accent">#R{reviewID}</span>
                    </div>
                    <div className="det-field">
                      <span className="det-lbl">Bác sĩ kết luận</span>
                      <span className="det-val">{reviewedByDoctorName ?? doctorFullName ?? '—'}</span>
                    </div>
                    <div className="det-field">
                      <span className="det-lbl">Ngày kết luận</span>
                      <span className="det-val">{fmtDateTime(reviewedAt)}</span>
                    </div>
                  </div>

                  <div className="review-block">
                    <div className="review-field review-full">
                      <span className="review-lbl">🔬 Chẩn đoán cuối cùng</span>
                      <div className="review-val">{finalDiagnosis}</div>
                    </div>
                    <div className="review-field">
                      <span className="review-lbl">💊 Phác đồ điều trị</span>
                      <div className="review-val">{treatmentPlan}</div>
                    </div>
                    <div className="review-field">
                      <span className="review-lbl">💡 Lời khuyên bác sĩ</span>
                      <div className="review-val">{doctorAdvice}</div>
                    </div>
                    {note && (
                        <div className="review-field review-full">
                          <span className="review-lbl">📎 Ghi chú thêm</span>
                          <div className="review-val">{note}</div>
                        </div>
                    )}
                  </div>
                </>
            ) : (
                <div className="no-review">
                  <span>⏳</span>
                  Chưa có kết luận từ bác sĩ cho phiên khám này.
                  <br />
                  <small style={{color:'var(--text-muted)'}}>Kết quả sẽ được cập nhật sau khi bác sĩ hoàn tất đánh giá.</small>
                </div>
            )}
          </Section>

          {/* Back */}
          <button className="back-btn" onClick={() => navigate('/medical-records')}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
            Quay lại danh sách
          </button>
        </div>
      </main>
  )
}
