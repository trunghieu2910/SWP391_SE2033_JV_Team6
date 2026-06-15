import { useState, useEffect, useRef, useCallback } from 'react'
import medicalRecordService from '../../services/medicalRecordService'
import MedicalRecordCard from '../../components/common/MedicalRecordCard.jsx'

const FILTER_OPTS = [
  { key: '',          label: 'Tất cả'     },
  { key: 'COMPLETED', label: 'Hoàn thành' },
  { key: 'PENDING',   label: 'Đang chờ'   },
]

const PAGE_SIZE = 10

export default function MedicalRecordsPage() {
  const [query,    setQuery]    = useState('')
  const [filter,   setFilter]   = useState('')
  const [records,  setRecords]  = useState([])
  const [page,     setPage]     = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)
  const inputRef = useRef(null)

  const fetchRecords = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await medicalRecordService.getRecords({
        keyword: query.trim() || undefined,
        status: filter || undefined,
        page,
        size: PAGE_SIZE,
      })
      const data = res.data
      setRecords(data.content ?? [])
      setTotalElements(data.totalElements ?? (data.content?.length ?? 0))
    } catch (err) {
      setError(err?.response?.data?.message || 'Không thể tải danh sách bệnh án.')
      setRecords([])
    } finally {
      setLoading(false)
    }
  }, [query, filter, page])

  useEffect(() => {
    fetchRecords()
  }, [fetchRecords])

  // Keyboard shortcut: Ctrl+K / Cmd+K
  useEffect(() => {
    const handler = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault()
        inputRef.current?.focus()
      }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [])

  // Reset to first page when filters change
  useEffect(() => {
    setPage(0)
  }, [query, filter])

  const total      = totalElements
  const completed  = records.filter(r => r.status === 'COMPLETED').length
  const pending    = records.filter(r => r.status === 'PENDING').length
  const withReview = records.filter(r => r.diagnosis && r.diagnosis !== 'Chưa có chẩn đoán').length

  const totalPages = Math.max(1, Math.ceil(totalElements / PAGE_SIZE))

  return (
    <main className="page z1">
      <div className="container">

        {/* Hero */}
        <div className="hero">
          <h1>🗂️ Hồ Sơ Bệnh Án</h1>
          <p>Tìm kiếm và xem chi tiết bệnh án bằng tên bệnh nhân hoặc số CCCD</p>
        </div>

        {/* Stats */}
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-icon blue">📋</div>
            <div>
              <div className="stat-val">{total}</div>
              <div className="stat-lbl">Tổng phiên khám</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon green">✅</div>
            <div>
              <div className="stat-val">{completed}</div>
              <div className="stat-lbl">Đã hoàn thành </div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon yellow">⏳</div>
            <div>
              <div className="stat-val">{pending}</div>
              <div className="stat-lbl">Đang chờ xử lý </div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon purple">📝</div>
            <div>
              <div className="stat-val">{withReview}</div>
              <div className="stat-lbl">Đã có kết luận </div>
            </div>
          </div>
        </div>

        {/* Search */}
        <div className="search-area">
          <div className="search-wrapper">
            <div className="search-box">
              <span className="search-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" strokeWidth="2">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="m21 21-4.35-4.35"/>
                </svg>
              </span>
              <input
                ref={inputRef}
                className="search-input"
                placeholder="Nhập tên bệnh nhân hoặc số CCCD (12 chữ số)..."
                value={query}
                onChange={e => setQuery(e.target.value)}
              />
              {query && (
                <button className="search-clear" onClick={() => setQuery('')} title="Xóa">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" strokeWidth="2.5">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              )}
              <button className="search-btn" onClick={fetchRecords}>Tìm kiếm</button>
            </div>

          </div>
        </div>

        {/* Filter bar */}
        <div className="filter-bar">
          <div className="filter-tabs">
            {FILTER_OPTS.map(opt => (
              <button
                key={opt.key || 'ALL'}
                className={`filter-tab ${filter === opt.key ? 'active' : ''}`}
                onClick={() => setFilter(opt.key)}
              >
                {opt.label}
              </button>
            ))}
          </div>
          <div className="results-info">
            Tìm thấy <strong>{totalElements}</strong> bệnh án
            {query && <> cho "<strong>{query}</strong>"</>}
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="empty">
            <div className="empty-icon">⚠️</div>
            <h3>Đã có lỗi xảy ra</h3>
            <p>{error}</p>
          </div>
        )}

        {/* Loading */}
        {!error && loading && (
          <div className="empty">
            <div className="empty-icon">⏳</div>
            <h3>Đang tải dữ liệu...</h3>
          </div>
        )}

        {/* Cards */}
        {!error && !loading && (
          <div className="records-grid">
            {records.length > 0
              ? records.map(rec => (
                  <MedicalRecordCard key={rec.id} record={rec} />
                ))
              : (
                <div className="empty">
                  <div className="empty-icon">🔍</div>
                  <h3>Không tìm thấy bệnh án</h3>
                  <p>Thử tìm bằng tên đầy đủ hoặc số CCCD 12 chữ số</p>
                </div>
              )
            }
          </div>
        )}

        {/* Pagination */}
        {!error && !loading && totalPages > 0 && (
          <div className="filter-bar" style={{ justifyContent: 'center', gap: 12 }}>
            <button
              className="filter-tab"
              disabled={page === 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
            >
              ← Trang trước
            </button>
            <div className="results-info">
              Trang {page + 1} / {totalPages}
            </div>
            <button
              className="filter-tab"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            >
              Trang sau →
            </button>
          </div>
        )}

      </div>
    </main>
  )
}
