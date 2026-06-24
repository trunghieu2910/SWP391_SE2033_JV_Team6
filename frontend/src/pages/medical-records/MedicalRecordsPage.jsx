import { useState, useEffect, useRef, useCallback } from 'react'
import medicalRecordService from '../../services/medicalRecordService'
import MedicalRecordCard from './MedicalRecordCard'

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
    <main className="p-6 max-w-7xl mx-auto space-y-6 text-left animate-fade-in">
      {/* Hero Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-100 pb-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            📁 Hồ Sơ Bệnh Án
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Tìm kiếm và xem chi tiết bệnh án bằng tên bệnh nhân hoặc số CCCD
          </p>
        </div>
      </div>

      {/* Stats Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Stat Card 1 */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 transition-all hover:shadow-md">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl text-2xl">📋</div>
          <div>
            <div className="text-2xl font-bold text-gray-900">{total}</div>
            <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Tổng phiên khám</div>
          </div>
        </div>
        {/* Stat Card 2 */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 transition-all hover:shadow-md">
          <div className="p-3 bg-green-50 text-green-600 rounded-xl text-2xl">✅</div>
          <div>
            <div className="text-2xl font-bold text-gray-900">{completed}</div>
            <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Đã hoàn thành</div>
          </div>
        </div>
        {/* Stat Card 3 */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 transition-all hover:shadow-md">
          <div className="p-3 bg-yellow-50 text-yellow-600 rounded-xl text-2xl">⏳</div>
          <div>
            <div className="text-2xl font-bold text-gray-900">{pending}</div>
            <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Đang chờ xử lý</div>
          </div>
        </div>
        {/* Stat Card 4 */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 transition-all hover:shadow-md">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl text-2xl">📝</div>
          <div>
            <div className="text-2xl font-bold text-gray-900">{withReview}</div>
            <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Đã có kết luận</div>
          </div>
        </div>
      </div>

      {/* Search & Filter Section */}
      <div className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm space-y-4">
        <div className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-400">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8"/>
                <path d="m21 21-4.35-4.35"/>
              </svg>
            </span>
            <input
              ref={inputRef}
              className="w-full pl-10 pr-10 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
              placeholder="Nhập tên bệnh nhân hoặc số CCCD (12 chữ số)... Nhấn Ctrl+K để tìm"
              value={query}
              onChange={e => setQuery(e.target.value)}
            />
            {query && (
              <button 
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                onClick={() => setQuery('')}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            )}
          </div>
          <button 
            className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl text-sm shadow-sm transition-all shrink-0"
            onClick={fetchRecords}
          >
            Tìm kiếm
          </button>
        </div>

        {/* Filters and Search Summary */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2 border-t border-gray-100">
          <div className="flex gap-2">
            {FILTER_OPTS.map(opt => (
              <button
                key={opt.key || 'ALL'}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition-all border ${
                  filter === opt.key 
                    ? 'bg-blue-600 border-blue-600 text-white shadow-sm'
                    : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                }`}
                onClick={() => setFilter(opt.key)}
              >
                {opt.label}
              </button>
            ))}
          </div>
          <div className="text-xs text-gray-500 font-medium">
            Tìm thấy <strong className="text-gray-900 font-bold">{totalElements}</strong> bệnh án
            {query && <> cho "<strong>{query}</strong>"</>}
          </div>
        </div>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-white rounded-2xl border border-dashed border-red-200 p-8 text-center flex flex-col items-center justify-center">
          <div className="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center text-red-600 text-xl mb-3">⚠️</div>
          <h3 className="text-base font-bold text-gray-800">Đã có lỗi xảy ra</h3>
          <p className="text-xs text-gray-500 mt-1">{error}</p>
        </div>
      )}

      {/* Loading State */}
      {!error && loading && (
        <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center flex flex-col items-center justify-center min-h-[300px]">
          <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-sm text-gray-500 mt-4 font-medium">Đang tải dữ liệu...</span>
        </div>
      )}

      {/* Record Cards Grid */}
      {!error && !loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {records.length > 0 ? (
            records.map(rec => (
              <MedicalRecordCard key={rec.id} record={rec} />
            ))
          ) : (
            <div className="col-span-full bg-white rounded-2xl border border-dashed border-gray-200 p-12 text-center flex flex-col items-center justify-center min-h-[250px]">
              <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 text-xl mb-3">🔍</div>
              <h3 className="text-base font-bold text-gray-800">Không tìm thấy bệnh án</h3>
              <p className="text-xs text-gray-500 mt-1 max-w-[280px]">Thử tìm bằng tên đầy đủ hoặc số CCCD 12 chữ số.</p>
            </div>
          )}
        </div>
      )}

      {/* Pagination Footer */}
      {!error && !loading && totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 pt-6 border-t border-gray-100">
          <button
            className={`px-4 py-2 border rounded-xl text-xs font-bold transition-all ${
              page === 0 
                ? 'bg-gray-50 border-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
            }`}
            disabled={page === 0}
            onClick={() => setPage(p => Math.max(0, p - 1))}
          >
            ← Trang trước
          </button>
          <div className="text-xs text-gray-500 font-bold">
            Trang {page + 1} / {totalPages}
          </div>
          <button
            className={`px-4 py-2 border rounded-xl text-xs font-bold transition-all ${
              page >= totalPages - 1 
                ? 'bg-gray-50 border-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
            }`}
            disabled={page >= totalPages - 1}
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
          >
            Trang sau →
          </button>
        </div>
      )}
    </main>
  )
}
