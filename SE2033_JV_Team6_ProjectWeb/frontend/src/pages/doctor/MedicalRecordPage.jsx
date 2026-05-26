import React, { useState } from 'react';
import useMedicalRecords from './useMedicalRecords';

const MedicalRecordPage = () => {
    const { records, loading, currentUser } = useMedicalRecords();


    const [selectedRecord, setSelectedRecord] = useState(null);

    if (loading) return <div style={styles.loading}>Đang tải danh sách bệnh án...</div>;
    if (!currentUser) return <div style={styles.loading}> Vui lòng đăng nhập!</div>;

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>
                {currentUser.role === 'DOCTOR'
                    ? ' QUẢN LÝ TẤT CẢ BỆNH ÁN (DÀNH CHO BÁC SĨ)'
                    : ' LỊCH SỬ BỆNH ÁN CỦA BẠN'}
            </h2>

            <p style={styles.welcome}>Xin chào, <strong>{currentUser.name}</strong> ({currentUser.role})</p>
            <p style={{color: '#95a5a6', fontStyle: 'italic'}}></p>

            {/* BẢNG DANH SÁCH RÚT GỌN */}
            <table style={styles.table}>
                <thead>
                <tr style={styles.thRow}>
                    <th style={styles.th}>Mã BA</th>
                    {currentUser.role === 'DOCTOR' && <th style={styles.th}>Tên Bệnh Nhân</th>}
                    <th style={styles.th}>Chẩn Đoán</th>
                    <th style={styles.th}>Ngày Khám</th>
                    <th style={styles.th}>Hành động</th>
                </tr>
                </thead>
                <tbody>
                {records.length > 0 ? (
                    records.map((item) => (
                        <tr
                            key={item.id}
                            style={styles.tr}
                            onClick={() => setSelectedRecord(item)}
                        >
                            <td style={styles.td}>BA-{item.id}</td>
                            {currentUser.role === 'DOCTOR' && <td style={styles.td}>{item.patientName || 'N/A'}</td>}
                            <td style={styles.td}><strong>{item.diagnosis}</strong></td>
                            <td style={styles.td}>{item.visitDate}</td>
                            <td style={styles.td}>
                                <button style={styles.viewBtn}>Xem chi tiết</button>
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan={currentUser.role === 'DOCTOR' ? 5 : 4} style={styles.noData}>
                            Không tìm thấy dữ liệu bệnh án nào.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>

            {}
            {selectedRecord && (
                <div style={styles.modalOverlay}>
                    <div style={styles.modalContent}>
                        <div style={styles.modalHeader}>
                            <h3> CHI TIẾT BỆNH ÁN TOÀN DIỆN</h3>
                            <button style={styles.closeBtn} onClick={() => setSelectedRecord(null)}> Đóng</button>
                        </div>

                        <div style={styles.modalBody}>
                            <div style={styles.infoGroup}>
                                <p><strong>Mã Bệnh Án:</strong> BA-{selectedRecord.id}</p>
                                <p><strong>Bệnh Nhân:</strong> {selectedRecord.patientName || currentUser.name}</p>
                                <p><strong>Ngày Khám:</strong> {selectedRecord.visitDate}</p>
                            </div>
                            <hr style={styles.hr}/>

                            <p style={styles.detailItem}><strong> Triệu chứng lâm sàng:</strong></p>
                            <div style={styles.detailBox}>{selectedRecord.symptoms}</div>

                            <p style={styles.detailItem}><strong> Kết luận chẩn đoán:</strong></p>
                            <div style={styles.detailBox}>{selectedRecord.diagnosis}</div>

                            {}
                            <p style={styles.detailItem}><strong> Đơn thuốc chỉ định:</strong></p>
                            <div style={styles.detailBox}>{selectedRecord.prescription || "Paracetamol 500mg x 10 viên (Ngày uống 2 lần), Amoxicillin 500mg x 14 viên"}</div>

                            <p style={styles.detailItem}><strong> Lời dặn của Bác sĩ:</strong></p>
                            <div style={styles.detailBox}>{selectedRecord.doctorNotes || "Uống thuốc đúng giờ, ăn đồ ấm, nghỉ ngơi hợp lý và tránh làm việc nặng."}</div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};


const styles = {
    container: { padding: '30px', fontFamily: 'Arial, sans-serif', maxWidth: '1000px', margin: '0 auto' },
    title: { color: '#2c3e50', borderBottom: '2px solid #3498db', paddingBottom: '10px' },
    welcome: { color: '#7f8c8d', marginBottom: '20px' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '10px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' },
    thRow: { backgroundColor: '#3498db' },
    th: { color: '#fff', padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' },
    tr: { backgroundColor: '#fff', borderBottom: '1px solid #ddd', cursor: 'pointer', transition: '0.2s' },
    td: { padding: '12px', color: '#333' },
    viewBtn: { backgroundColor: '#2ecc71', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer' },
    noData: { textAlign: 'center', padding: '20px', color: '#e74c3c', fontStyle: 'italic' },
    loading: { textAlign: 'center', marginTop: '50px', fontSize: '18px', fontWeight: 'bold', color: '#3498db' },

    // CSS CSS cho phần Khuôn Popup (Modal)
    modalOverlay: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { backgroundColor: 'white', width: '600px', borderRadius: '8px', padding: '20px', boxShadow: '0 5px 15px rgba(0,0,0,0.3)', animation: 'fadeIn 0.3s' },
    modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #2c3e50', paddingBottom: '10px' },
    closeBtn: { backgroundColor: 'transparent', border: 'none', fontSize: '16px', cursor: 'pointer', color: '#e74c3c', fontWeight: 'bold' },
    modalBody: { marginTop: '15px' },
    infoGroup: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', color: '#34495e' },
    hr: { margin: '15px 0', border: 'none', borderTop: '1px solid #eee' },
    detailItem: { marginBottom: '5px', color: '#2c3e50', fontWeight: 'bold' },
    detailBox: { backgroundColor: '#f8f9fa', padding: '10px', borderRadius: '4px', borderLeft: '4px solid #3498db', marginBottom: '15px', color: '#555', fontSize: '14px' }
};

export default MedicalRecordPage;