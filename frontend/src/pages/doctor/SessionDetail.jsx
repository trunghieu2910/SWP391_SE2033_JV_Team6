import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    FaArrowLeft, FaUserCircle, FaCalendarAlt, FaVenusMars,
    FaMapMarkerAlt, FaWeight, FaRulerVertical, FaStethoscope,
    FaImage, FaRobot, FaChevronDown, FaChevronUp,
    FaHistory, FaChartLine,
} from 'react-icons/fa';
import toast, { Toaster } from 'react-hot-toast';
import doctorService from '../../services/doctorService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatusBadge from '../../components/common/StatusBadge';
import AddLabResultModal from '../../components/doctor/session-detail/AddLabResultModal';
import ConfirmModal from '../../components/common/ConfirmModal';
import ClinicalSymptomsForm from '../../components/doctor/session-detail/ClinicalSymptomsForm';

// BUG FIX: import component tách riêng thay vì nhúng toàn bộ logic LIS ở đây
import LabResultsSection from '../../components/doctor/session-detail/LabResults';

const SessionDetail = () => {
    const { sessionId } = useParams();
    const navigate = useNavigate();

    const [session, setSession] = useState(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);

    // Accordion states
    const [openSymptom, setOpenSymptom] = useState(false);
    const [openImage, setOpenImage] = useState(false);

    // Modal states
    const [showShareModal, setShowShareModal] = useState(false);
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedStatus, setSelectedStatus] = useState('');
    const [showAddLabModal, setShowAddLabModal] = useState(false);

    useEffect(() => {
        fetchSessionDetail();
    }, [sessionId]);

    const fetchSessionDetail = async () => {
        setLoading(true);
        try {
            const response = await doctorService.getSessionDetail(sessionId);
            setSession(response.data);
        } catch (error) {
            toast.error('Không thể tải thông tin ca chẩn đoán');
            navigate('/doctor/dashboard');
        } finally {
            setLoading(false);
        }
    };

    const refreshSessionDetail = async () => {
        try {
            const response = await doctorService.getSessionDetail(sessionId);
            setSession(response.data);
        } catch (error) {
            toast.error('Không thể tải thông tin ca chẩn đoán');
        }
    };

    const handleUpdateShare = async () => {
        if (!session) return;
        setUpdating(true);
        try {
            await doctorService.updateSessionShare({
                sessionId: session.sessionId,
                isShared: !session.isShared,
            });
            toast.success(session.isShared ? 'Đã gỡ công bố' : 'Đã công bố ca chẩn đoán');
            setSession({ ...session, isShared: !session.isShared });
            setShowShareModal(false);
        } catch {
            toast.error('Không thể cập nhật trạng thái công bố');
        } finally {
            setUpdating(false);
        }
    };

    const handleUpdateStatus = async () => {
        if (!session || !selectedStatus) return;
        setUpdating(true);
        try {
            await doctorService.updateSessionStatus({
                sessionId: session.sessionId,
                status: selectedStatus,
            });
            toast.success(`Đã chuyển trạng thái sang ${getStatusLabel(selectedStatus)}`);
            setSession({ ...session, status: selectedStatus });
            setShowStatusModal(false);
            setSelectedStatus('');
        } catch {
            toast.error('Không thể cập nhật trạng thái');
        } finally {
            setUpdating(false);
        }
    };

    const getStatusLabel = (statusValue) => {
        const map = {
            PENDING: 'Chờ xử lý',
            PROCESSING: 'Đang xử lý',
            COMPLETED: 'Hoàn thành',
            FAILED: 'Thất bại',
        };
        return map[statusValue] || statusValue;
    };

    const statusOptions = [
        { value: 'PENDING',    label: 'Chờ xử lý' },
        { value: 'PROCESSING', label: 'Đang xử lý' },
        { value: 'COMPLETED',  label: 'Hoàn thành' },
        { value: 'FAILED',     label: 'Thất bại' },
    ];

    const formatDate     = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';
    const formatDateTime = (d) => d ? new Date(d).toLocaleString('vi-VN')    : '—';

    if (loading) return <LoadingSpinner />;
    if (!session) return null;

    return (
        <div className="p-6">
            <Toaster position="top-right" />
            {/* ── Tiêu đề ── */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-4">
                    <button
                        onClick={() => navigate('/doctor/dashboard')}
                        className="flex items-center gap-2 text-gray-500 hover:text-[#100357] transition"
                    >
                        <FaArrowLeft className="w-4 h-4" /> Quay lại
                    </button>
                    <h1 className="text-2xl font-bold text-gray-800">
                        Chi tiết ca chẩn đoán #{session.sessionId}
                    </h1>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={() => setShowStatusModal(true)}
                        className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90 transition"
                    >
                        Đổi trạng thái
                    </button>
                    {session.status === 'COMPLETED' ? (
                        <button
                            onClick={() => setShowShareModal(true)}
                            className={`px-4 py-2 rounded-lg transition ${
                                session.isShared
                                    ? 'bg-green-100 text-green-700 hover:bg-green-200'
                                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }`}
                        >
                            {session.isShared ? 'Đã công bố' : 'Chưa công bố'}
                        </button>
                    ) : (
                        <button
                            disabled
                            title="Chỉ có thể công bố khi ca chẩn đoán hoàn thành"
                            className="px-4 py-2 rounded-lg bg-gray-100 text-gray-400 cursor-not-allowed"
                        >
                            {session.isShared ? 'Đã công bố' : 'Chưa thể công bố'}
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* ── Cột trái: hồ sơ bệnh nhân ── */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
                        <div className="flex items-center gap-3 mb-4 pb-3 border-b border-gray-100">
                            <FaUserCircle className="text-3xl text-[#100357]" />
                            <div>
                                <h3 className="font-semibold text-gray-800">Hồ sơ bệnh nhân</h3>
                                <p className="text-xs text-gray-400">Thông tin chi tiết</p>
                            </div>
                        </div>
                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Mã ca</p>
                                    <p className="font-semibold text-gray-800">#{session.sessionId}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Mã bệnh nhân</p>
                                    <p className="font-semibold text-gray-800">#{session.patientId}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-xs text-gray-400 uppercase">Tên bệnh nhân</p>
                                <p className="font-semibold text-gray-800">{session.patientName || '—'}</p>
                            </div>
                            <div className="grid grid-cols-2 gap-3">
                                <div className="flex items-center gap-2">
                                    <FaCalendarAlt className="text-gray-400 text-xs" />
                                    <div>
                                        <p className="text-xs text-gray-400 uppercase">Ngày sinh</p>
                                        <p className="text-sm text-gray-800">{formatDate(session.patientDob)}</p>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <FaVenusMars className="text-gray-400 text-xs" />
                                    <div>
                                        <p className="text-xs text-gray-400 uppercase">Giới tính</p>
                                        <p className="text-sm text-gray-800">
                                            {session.patientGender === 'Male' ? 'Nam'
                                                : session.patientGender === 'Female' ? 'Nữ' : '—'}
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <FaMapMarkerAlt className="text-gray-400 text-xs" />
                                <div>
                                    <p className="text-xs text-gray-400 uppercase">Địa chỉ</p>
                                    <p className="text-sm text-gray-800">{session.patientAddress || '—'}</p>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-3 pt-2 border-t border-gray-100">
                                <div className="flex items-center gap-2">
                                    <FaRulerVertical className="text-gray-400 text-xs" />
                                    <div>
                                        <p className="text-xs text-gray-400 uppercase">Chiều cao</p>
                                        <p className="font-medium text-gray-800">
                                            {session.height ? `${session.height} cm` : '—'}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <FaWeight className="text-gray-400 text-xs" />
                                    <div>
                                        <p className="text-xs text-gray-400 uppercase">Cân nặng</p>
                                        <p className="font-medium text-gray-800">
                                            {session.weight ? `${session.weight} kg` : '—'}
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <div className="pt-2">
                                <p className="text-xs text-gray-400 uppercase">Bác sĩ phụ trách</p>
                                <p className="text-sm text-gray-800">{session.doctorName || '—'}</p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-400 uppercase">Ngày tạo</p>
                                <p className="text-sm text-gray-800">{formatDateTime(session.createdAt)}</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* ── Cột phải: nội dung chính ── */}
                <div className="lg:col-span-2">
                    <div className="space-y-4">
                        {/* Thống kê nhanh */}
                        <div className="grid grid-cols-3 gap-4">
                            <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100 text-center">
                                <FaHistory className="text-2xl text-[#100357] mx-auto mb-2" />
                                <p className="text-2xl font-bold text-gray-800">
                                    {session.labResults?.length || 0}
                                </p>
                                <p className="text-xs text-gray-400">Xét nghiệm</p>
                            </div>
                            <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100 text-center">
                                <FaImage className="text-2xl text-[#100357] mx-auto mb-2" />
                                <p className="text-2xl font-bold text-gray-800">
                                    {session.medicalImages?.length || 0}
                                </p>
                                <p className="text-xs text-gray-400">Hình ảnh</p>
                            </div>
                            <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100 text-center">
                                <FaChartLine className="text-2xl text-[#100357] mx-auto mb-2" />
                                <p className="text-2xl font-bold text-gray-800">
                                    {session.status === 'COMPLETED' ? '✅'
                                        : session.status === 'PROCESSING' ? '🔄' : '⏳'}
                                </p>
                                <p className="text-xs text-gray-400">{getStatusLabel(session.status)}</p>
                            </div>
                        </div>

                        {/* Triệu chứng lâm sàng */}
                        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                            <button
                                onClick={() => setOpenSymptom(prev => !prev)}
                                className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 transition"
                            >
                                <div className="flex items-center gap-2">
                                    <FaStethoscope className="text-[#100357]" />
                                    <span className="font-semibold text-gray-800">Triệu chứng lâm sàng</span>
                                    <span className="text-xs bg-gray-200 px-2 py-0.5 rounded-full">Chi tiết</span>
                                </div>
                                {openSymptom ? <FaChevronUp /> : <FaChevronDown />}
                            </button>
                            {openSymptom && (
                                <ClinicalSymptomsForm
                                    sessionId={session.sessionId}
                                    initialData={{
                                        height: session.height,
                                        weight: session.weight,
                                        ...session.symptomResult,
                                    }}
                                    onSave={async (data) => {
                                        try {
                                            await doctorService.updateSessionSymptoms(session.sessionId, data);
                                            toast.success('Đã lưu triệu chứng lâm sàng');
                                            await fetchSessionDetail();
                                        } catch {
                                            toast.error('Không thể lưu triệu chứng');
                                        }
                                    }}
                                />
                            )}
                        </div>

                        {/* Xét nghiệm y tế — dùng component tách riêng */}
                        <LabResultsSection
                            labResults={session.labResults}
                            onAddClick={() => setShowAddLabModal(true)}
                            onRefresh={fetchSessionDetail}
                        />

                        {/* Hình ảnh y tế */}
                        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                            <button
                                onClick={() => setOpenImage(prev => !prev)}
                                className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 transition"
                            >
                                <div className="flex items-center gap-2">
                                    <FaImage className="text-[#100357]" />
                                    <span className="font-semibold text-gray-800">Hình ảnh y tế</span>
                                    <span className="text-xs bg-gray-200 px-2 py-0.5 rounded-full">
                                        {session.medicalImages?.length || 0}
                                    </span>
                                </div>
                                {openImage ? <FaChevronUp /> : <FaChevronDown />}
                            </button>
                            {openImage && (
                                <div className="p-4 border-t border-gray-200">
                                    {session.medicalImages?.length > 0 ? (
                                        <div className="space-y-4">
                                            {session.medicalImages.map((img, idx) => (
                                                <div key={idx} className="border border-gray-100 rounded-lg p-3">
                                                    <div className="flex justify-between items-start mb-2">
                                                        <div>
                                                            <p className="font-medium text-gray-800">{img.imageType}</p>
                                                            <p className="text-xs text-gray-400">{formatDateTime(img.createdAt)}</p>
                                                        </div>
                                                        <StatusBadge type="status" value={img.status} />
                                                    </div>
                                                    {img.images?.length > 0 ? (
                                                        <div className="grid grid-cols-3 gap-2 mt-2">
                                                            {img.images.map((detail, dIdx) => (
                                                                <img
                                                                    key={dIdx}
                                                                    src={detail.imageUrl}
                                                                    alt={`Medical ${dIdx + 1}`}
                                                                    className="w-full h-20 object-cover rounded-lg cursor-pointer"
                                                                    onClick={() => window.open(detail.imageUrl, '_blank')}
                                                                />
                                                            ))}
                                                        </div>
                                                    ) : img.status === 'PENDING' ? (
                                                        <p className="text-gray-400 text-sm mt-2">Chưa có hình ảnh chi tiết</p>
                                                    ) : null}
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="text-gray-400 text-center py-4">Chưa có hình ảnh y tế nào</p>
                                    )}
                                    <button
                                        onClick={() => toast.info('Tính năng đang phát triển')}
                                        className="mt-4 w-full py-2 border border-dashed border-gray-300 rounded-lg
                                                   text-gray-500 hover:text-[#100357] hover:border-[#100357]
                                                   transition flex items-center justify-center gap-2"
                                    >
                                        <FaImage className="w-3 h-3" />
                                        Thêm hình ảnh y tế
                                    </button>
                                </div>
                            )}
                        </div>

                        {/* AI phân tích */}
                        <div className="bg-gradient-to-r from-[#100357] to-[#2a1a6e] rounded-xl shadow-sm p-5 text-white">
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-semibold text-lg">Hỗ trợ phân tích từ AI</h3>
                                    <p className="text-white/70 text-sm mt-1">
                                        AI sẽ phân tích tổng hợp dữ liệu để đề xuất chẩn đoán
                                    </p>
                                </div>
                                <button
                                    onClick={() => toast.info('Tính năng phân tích từ AI đang được phát triển')}
                                    className="px-5 py-2 bg-white/20 hover:bg-white/30 rounded-lg transition flex items-center gap-2"
                                >
                                    <FaRobot className="w-5 h-5" />
                                    Phân tích
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* ── Modals ── */}
            <ConfirmModal
                isOpen={showShareModal}
                title="Xác nhận thay đổi công bố"
                message={`Bạn có chắc chắn muốn ${session.isShared ? 'gỡ công bố' : 'công bố'} ca chẩn đoán #${session.sessionId}?`}
                confirmText={session.isShared ? 'Gỡ công bố' : 'Công bố'}
                cancelText="Hủy"
                onConfirm={handleUpdateShare}
                onClose={() => setShowShareModal(false)}
                isLoading={updating}
            />

            <AddLabResultModal
                isOpen={showAddLabModal}
                onClose={() => setShowAddLabModal(false)}
                sessionId={session.sessionId}
                onSuccess={refreshSessionDetail}
            />

            <ConfirmModal
                isOpen={showStatusModal}
                title="Xác nhận đổi trạng thái"
                message={
                    <div>
                        <p className="mb-3">
                            Bạn có chắc chắn muốn đổi trạng thái ca chẩn đoán{' '}
                            <strong>#{session.sessionId}</strong>?
                        </p>
                        <label className="block text-sm text-gray-600 mb-2">Chọn trạng thái mới:</label>
                        <select
                            value={selectedStatus}
                            onChange={(e) => setSelectedStatus(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                        >
                            <option value="">Chọn trạng thái</option>
                            {statusOptions.map(opt => (
                                <option key={opt.value} value={opt.value}>{opt.label}</option>
                            ))}
                        </select>
                    </div>
                }
                confirmText="Xác nhận"
                cancelText="Hủy"
                onConfirm={handleUpdateStatus}
                onClose={() => { setShowStatusModal(false); setSelectedStatus(''); }}
                isLoading={updating}
            />
        </div>
    );
};

export default SessionDetail;