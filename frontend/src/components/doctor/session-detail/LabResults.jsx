import React, { useState } from 'react';
import { FaFlask, FaChevronDown, FaChevronUp } from 'react-icons/fa';
import StatusBadge from '../../../components/common/StatusBadge';
import toast, { Toaster } from 'react-hot-toast';
import lisIntegrationService from '../../../services/lisIntegrationService';

// ── Kho dữ liệu mock LIS ──────────────────────────────────────────────
// Tách ra khỏi SessionDetail để dễ mở rộng sau này
const LIS_MOCK_DATA = {
    'Xét nghiệm tế bào học cổ tử cung': [
        { testName: 'Chất lượng mẫu bệnh phẩm', resultValue: 'Đạt yêu cầu (Có tế bào vùng chuyển tiếp)', unit: null },
        { testName: 'Tác nhân vi sinh', resultValue: 'Không phát hiện', unit: null },
        { testName: 'Đánh giá tế bào tuyến', resultValue: 'Bình thường', unit: null },
        { testName: 'Đánh giá tế bào vảy', resultValue: 'HSIL (Tổn thương mức độ cao)', unit: null },
        { testName: 'Kết luận (The Bethesda System)', resultValue: 'Bất thường tế bào biểu mô vảy (HSIL)', unit: null },
    ],
    'Xét nghiệm DNA của virus HPV': [
        { testName: 'Kiểm chứng nội bộ (IC)', resultValue: 'Hợp lệ', unit: null },
        { testName: 'Phát hiện HPV nguy cơ cao', resultValue: 'Positive (Dương tính)', unit: null },
        { testName: 'HPV Tuýp 16', resultValue: 'Positive', unit: null },
        { testName: 'HPV Tuýp 18', resultValue: 'Negative', unit: null },
        { testName: '12 tuýp nguy cơ cao khác', resultValue: 'Negative', unit: null },
        { testName: 'Ngưỡng chu kỳ (Ct Value) - Tuýp 16', resultValue: '22.4', unit: 'Chu kỳ' },
    ],
    'Định tuýp HPV nguy cơ cao': [
        { testName: 'Kiểm soát chất lượng (Internal Control)', resultValue: 'Đạt', unit: null },
        { testName: 'Tuýp 16', resultValue: 'Positive', unit: null },
        { testName: 'Tuýp 18', resultValue: 'Negative', unit: null },
        { testName: 'Tuýp 31', resultValue: 'Positive', unit: null },
        { testName: 'Tuýp 33', resultValue: 'Negative', unit: null },
        { testName: 'Tuýp 45', resultValue: 'Negative', unit: null },
        { testName: 'Tuýp 52', resultValue: 'Positive', unit: null },
        { testName: 'Tuýp 58', resultValue: 'Negative', unit: null },
        { testName: 'Các tuýp nguy cơ cao khác (35, 39, 51, 56, 59, 66, 68)', resultValue: 'Negative', unit: null },
        { testName: 'Tuýp 6', resultValue: 'Negative', unit: null },
        { testName: 'Tuýp 11', resultValue: 'Negative', unit: null },
        { testName: 'Kết luận Định tuýp', resultValue: 'Nhiễm đa tuýp nguy cơ cao (16, 31, 52)', unit: null },
    ],
    'Sinh thiết': [
        { testName: 'Mô tả đại thể', resultValue: 'Nhận 02 mảnh mô màu xám nhạt, kích thước lớn nhất 0.3x0.2cm.', unit: null },
        { testName: 'Mô tả vi thể', resultValue: 'Biểu mô vảy quá sản, tế bào mất phân cực, nhân quái, nhân chia ở 2/3 bề dày lớp biểu mô. Màng đáy còn nguyên vẹn.', unit: null },
        { testName: 'Kết luận Giải phẫu bệnh', resultValue: 'Tân sản nội biểu mô cổ tử cung độ 2 (CIN 2)', unit: null },
    ],
    'Dấu ấn ung thư SCC': [
        { testName: 'Phương pháp phân tích', resultValue: 'Miễn dịch hóa phát quang (CMIA)', unit: null },
        { testName: 'Mẫu bệnh phẩm', resultValue: 'Huyết thanh (Serum)', unit: null },
        { testName: 'Nồng độ SCC Antigen', resultValue: '12.40', unit: 'ng/mL' },
        { testName: 'Khoảng tham chiếu (Trị số BT)', resultValue: '< 1.50', unit: 'ng/mL' },
        { testName: 'Đánh giá kết quả', resultValue: 'Tăng cao', unit: null },
    ],
    'Xét nghiệm máu cơ bản': [
        { testName: 'Hồng cầu (RBC)', resultValue: '2.8 ↓', unit: 'T/L' },
        { testName: 'Huyết sắc tố (HGB)', resultValue: '85 ↓', unit: 'g/L' },
        { testName: 'Bạch cầu (WBC)', resultValue: '1.5 ↓', unit: 'G/L' },
        { testName: 'Tiểu cầu (PLT)', resultValue: '90 ↓', unit: 'G/L' },
        { testName: 'Ure máu', resultValue: '5.0', unit: 'mmol/L' },
        { testName: 'Creatinin máu', resultValue: '80', unit: 'µmol/L' },
        { testName: 'AST (Men gan)', resultValue: '85 ↑', unit: 'U/L' },
        { testName: 'ALT (Men gan)', resultValue: '90 ↑', unit: 'U/L' },
        { testName: 'Kết luận', resultValue: 'Thiếu máu, Giảm bạch cầu hạt, Tăng men gan', unit: null },
    ],
};

// ── LabResultsSection ─────────────────────────────────────────────────
/**
 * Props:
 *   labResults   - mảng LabResult từ session (session.labResults)
 *   onAddClick   - callback mở modal "Thêm xét nghiệm"
 *   onRefresh    - callback gọi lại fetchSessionDetail() để cập nhật UI từ DB
 */
const LabResultsSection = ({ labResults: initialLabResults, onAddClick, onRefresh }) => {
    const [open, setOpen] = useState(false);

    // BUG FIX: state cục bộ để cập nhật UI ngay lập tức sau khi nhận kết quả LIS
    // trước đây code ở SessionDetail gọi setLabResults (chưa khai báo) → crash âm thầm
    const [labResults, setLabResults] = useState(initialLabResults || []);
    const [simulatingId, setSimulatingId] = useState(null);

    // Đồng bộ khi parent reload (onRefresh)
    React.useEffect(() => {
        setLabResults(initialLabResults || []);
    }, [initialLabResults]);

    const formatDateTime = (date) => {
        if (!date) return '—';
        return new Date(date).toLocaleString('vi-VN');
    };

    // ── Handler lấy kết quả LIS ────────────────────────────────────
    const handleSimulateResult = async (labResultId, testType) => {
        setSimulatingId(labResultId);

        const mockResults = LIS_MOCK_DATA[testType] || [];

        if (mockResults.length === 0) {
            // BUG FIX: dùng toast thay vì addToast (chưa khai báo) → crash âm thầm
            toast.error(`Chưa có dữ liệu mẫu LIS cho loại xét nghiệm: "${testType}"`);
            setSimulatingId(null);
            return;
        }

        const payload = {
            labResultId,
            testResults: mockResults,
        };

        try {
            const res = await lisIntegrationService.sendResults(payload);
            const updatedResult = res.data?.data ?? res.data;

            // BUG FIX: dùng setLabResults cục bộ (đã khai báo ở trên)
            // thay vì setLabResults từ SessionDetail (chưa khai báo)
            setLabResults(prev =>
                prev.map(item =>
                    item.labResultId === labResultId
                        ? { ...item, ...updatedResult }
                        : item
                )
            );

            toast.success('Đã nhận kết quả xét nghiệm thành công!');

            // Tùy chọn: gọi onRefresh để đồng bộ hoàn toàn với DB
            // onRefresh?.();
        } catch (err) {
            const msg = err.response?.data?.message ?? 'Lỗi khi kết nối với máy xét nghiệm.';
            toast.error(msg);
        } finally {
            setSimulatingId(null);
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <Toaster position="top-right" />
            {/* ── Header accordion ── */}
            <button
                onClick={() => setOpen(prev => !prev)}
                className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 transition"
            >
                <div className="flex items-center gap-2">
                    <FaFlask className="text-[#100357]" />
                    <span className="font-semibold text-gray-800">Xét nghiệm y tế</span>
                    <span className="text-xs bg-gray-200 px-2 py-0.5 rounded-full">
                        {labResults.length}
                    </span>
                </div>
                {open ? <FaChevronUp /> : <FaChevronDown />}
            </button>

            {/* ── Nội dung (chỉ render khi mở) ── */}
            {open && (
                <div className="p-4 border-t border-gray-200">
                    {labResults.length > 0 ? (
                        <div className="space-y-4">
                            {labResults.map((lab, idx) => (
                                <div
                                    key={lab.labResultId ?? idx}
                                    className="border border-gray-100 rounded-lg p-3"
                                >
                                    {/* Header mỗi lab result */}
                                    <div className="flex justify-between items-start mb-2">
                                        <div>
                                            <p className="font-medium text-gray-800">{lab.testType}</p>
                                            <p className="text-xs text-gray-400">
                                                {formatDateTime(lab.createdAt)}
                                            </p>
                                        </div>

                                        <div className="flex items-center gap-3">
                                            {/* Nút lấy kết quả LIS — chỉ hiện khi PENDING */}
                                            {lab.status === 'PENDING' && (
                                                <button
                                                    type="button"
                                                    disabled={simulatingId === lab.labResultId}
                                                    onClick={() =>
                                                        handleSimulateResult(lab.labResultId, lab.testType)
                                                    }
                                                    className="px-3 py-1 bg-[#100357] text-white text-xs rounded
                                                               hover:bg-[#100357]/90 transition disabled:opacity-50
                                                               flex items-center gap-1"
                                                >
                                                    {simulatingId === lab.labResultId
                                                        ? <><span className="animate-spin">⏳</span> Đang chạy máy...</>
                                                        : 'Lấy kết quả LIS'}
                                                </button>
                                            )}
                                            <StatusBadge type="status" value={lab.status} />
                                        </div>
                                    </div>

                                    {/* Danh sách parameters */}
                                    {lab.parameters && lab.parameters.length > 0 ? (
                                        <div className="mt-2 space-y-1">
                                            {lab.parameters.map((param, pIdx) => (
                                                <div key={pIdx} className="flex gap-10 text-sm py-1 border-b border-gray-200 last:border-0">
                                                    <span className="text-gray-600 shrink-0 w-60 break-words">{param.parameterName}</span>
                                                    <span className="font-medium text-gray-800 break-words min-w-0">
                                                        {param.value}{param.unit && param.unit.trim() ? ` ${param.unit}` : ''}
                                                    </span>
                                                </div>
                                            ))}
                                        </div>
                                    ) : lab.status === 'PENDING' ? (
                                        <p className="text-gray-400 text-sm mt-2">
                                            Chưa có kết quả chi tiết
                                        </p>
                                    ) : null}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-gray-400 text-center py-4">Chưa có xét nghiệm nào</p>
                    )}

                    {/* Nút thêm xét nghiệm */}
                    <button
                        onClick={onAddClick}
                        className="mt-4 w-full py-2 border border-dashed border-gray-300 rounded-lg
                                   text-gray-500 hover:text-[#100357] hover:border-[#100357]
                                   transition flex items-center justify-center gap-2"
                    >
                        <FaFlask className="w-3 h-3" />
                        Thêm xét nghiệm
                    </button>
                </div>
            )}
        </div>
    );
};

export default LabResultsSection;