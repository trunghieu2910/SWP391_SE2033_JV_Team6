export default function StatusBadge({ status }) {
  const map = {
    COMPLETED: { cls: 'completed', label: 'Hoàn thành' },
    PENDING:   { cls: 'pending',   label: 'Đang chờ'   },
    PROCESSING:{ cls: 'pending',   label: 'Đang xử lý' },
    FAILED:    { cls: 'cancelled', label: 'Thất bại'   },
    CANCELLED: { cls: 'cancelled', label: 'Đã hủy'     },
    CANCELED:  { cls: 'cancelled', label: 'Đã hủy'     },
    CANCEL:    { cls: 'cancelled', label: 'Đã hủy'     },
  }
  const { cls, label } = map[String(status).toUpperCase()] ?? { cls: 'pending', label: status ?? '—' }
  return (
    <span className={`badge ${cls}`}>
      <span className="badge-dot" />
      {label}
    </span>
  )
}
