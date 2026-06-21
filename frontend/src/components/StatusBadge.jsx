export default function StatusBadge({ status }) {
  const map = {
    PENDING:    'badge-pending',
    COMPLETED:  'badge-completed',
    ACTIVE:     'badge-active',
    BLOCKED:    'badge-blocked',
    PROCESSING: 'badge-active',
  };
  const cls = map[status?.toUpperCase()] ?? 'badge-active';
  return <span className={`badge ${cls}`}>{status}</span>;
}
