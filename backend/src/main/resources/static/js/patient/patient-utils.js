/* Patient utilities for templates (served from backend static resources) */
const PatientUtils = {
  formatDate: function(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN');
  },
  formatDateTime: function(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleString('vi-VN');
  },
  getInitials: function(name){ if(!name) return '?'; const parts = name.trim().split(' '); return (parts[0][0] + (parts[parts.length-1][0]||'')).toUpperCase(); },
  showNotification: function(message, type='info'){ const cls = type==='error' ? 'alert-error' : 'alert-success'; const div = document.createElement('div'); div.className = 'alert '+cls; div.textContent = message; (document.querySelector('.container')||document.body).prepend(div); setTimeout(()=>div.remove(),4000); },
  getStatusBadgeHTML: function(status){ const m={PENDING:['⏳','Chờ xử lý','badge-pending'],IN_PROGRESS:['⚙️','Đang xử lý','badge-in-progress'],COMPLETED:['✅','Hoàn thành','badge-completed']}; const info=m[status]||['❓',status,'']; return `<span class="badge ${info[2]}">${info[0]} ${info[1]}</span>` },
  navigateToRecord: function(id){ window.location.href = '/patient/medical-record/' + id; },
  validateForm: function(form){ let ok=true; form.querySelectorAll('[required]').forEach(f=>{ if(!f.value.trim()){ f.classList.add('error'); ok=false } else f.classList.remove('error') }); return ok; },
  submitForm: function(form, endpoint, method='POST'){ if(!this.validateForm(form)){ this.showNotification('Vui lòng điền đầy đủ thông tin','error'); return false } const data = Object.fromEntries(new FormData(form)); fetch(endpoint,{method,headers:{'Content-Type':'application/json'},body:JSON.stringify(data)}) .then(r=>{ if(!r.ok) throw new Error('Network error'); return r.json() }) .then(()=>{ this.showNotification('Thành công!','success'); setTimeout(()=>window.location.href='/patient/home',1200) }) .catch(e=> this.showNotification('Lỗi: '+(e.message||e),'error')); return false }
};

if(typeof module !== 'undefined' && module.exports) module.exports = PatientUtils;
