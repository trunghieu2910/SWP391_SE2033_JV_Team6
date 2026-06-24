import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaPhone, FaKey, FaCheck, FaArrowLeft, FaClock, FaIdCard, FaUpload, FaFileImage } from 'react-icons/fa';
import adminService from '../../services/adminService';
import Topbar from '../../components/admin/layout/Topbar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const CreateDoctor = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [timeLeft, setTimeLeft] = useState(0);
    const [canResend, setCanResend] = useState(false);
    const timerRef = useRef(null);
    const otpInputsRef = useRef([]);
    const [certificateFile, setCertificateFile] = useState(null);
    const [certificatePreview, setCertificatePreview] = useState(null);

    const [formData, setFormData] = useState({
        userName: '',
        fullName: '',
        email: '',
        phoneNumber: '',
        nationalId: '',
    });
    const [otp, setOtp] = useState(['', '', '', '', '', '']);

    // Dọn timer khi component unmount
    useEffect(() => {
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, []);

    // Bộ đếm thời gian OTP (2 phút = 120 giây)
    const startTimer = (seconds = 120) => {
        if (timerRef.current) clearInterval(timerRef.current);

        setTimeLeft(seconds);
        setCanResend(false);

        timerRef.current = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timerRef.current);
                    setCanResend(true);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const getProgressPercentage = () => {
        return ((120 - timeLeft) / 120) * 100;
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setError('');
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Kiểm tra định dạng file
            const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/pdf'];
            if (!allowedTypes.includes(file.type)) {
                toast.error('Chỉ chấp nhận file ảnh (JPG, PNG) hoặc PDF');
                e.target.value = '';
                return;
            }
            // Kiểm tra kích thước (tối đa 5MB)
            if (file.size > 5 * 1024 * 1024) {
                toast.error('Kích thước file tối đa 5MB');
                e.target.value = '';
                return;
            }

            setCertificateFile(file);
            // Tạo preview cho ảnh
            const reader = new FileReader();
            reader.onloadend = () => {
                setCertificatePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleRemoveFile = () => {
        setCertificateFile(null);
        setCertificatePreview(null);
        document.getElementById('certificateInput').value = '';
    };

    const handleOtpChange = (index, value) => {
        if (value.length > 1) return;
        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);
        setError('');

        if (value && index < 5) {
            otpInputsRef.current[index + 1]?.focus();
        }
    };

    const handleOtpKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            const newOtp = [...otp];
            newOtp[index - 1] = '';
            setOtp(newOtp);
            otpInputsRef.current[index - 1]?.focus();
            setError('');
        }
    };

    const resetOtpFields = () => {
        setOtp(['', '', '', '', '', '']);
        otpInputsRef.current[0]?.focus();
    };

    const handleInitiate = async (e) => {
        e.preventDefault();

        const phoneRegex = /^(0|\+84)[0-9]{9,10}$/;
        if (!phoneRegex.test(formData.phoneNumber)) {
            setError('Số điện thoại không hợp lệ. Phải là số điện thoại Việt Nam (0xxxxxxxxx hoặc +84xxxxxxxxx)');
            return;
        }

        if (formData.nationalId && (formData.nationalId.length < 9 || formData.nationalId.length > 12)) {
            setError('CCCD/CMND phải có từ 9 đến 12 ký tự');
            return;
        }

        setLoading(true);
        setError('');
        try {
            const formDataToSend = new FormData();
            formDataToSend.append('userName', formData.userName);
            formDataToSend.append('fullName', formData.fullName);
            formDataToSend.append('email', formData.email);
            formDataToSend.append('phoneNumber', formData.phoneNumber);
            formDataToSend.append('nationalId', formData.nationalId || '');
            if (certificateFile) {
                formDataToSend.append('certificateFile', certificateFile);
            }

            await adminService.initiateCreateDoctor(formDataToSend);

            toast.success('OTP đã được gửi đến email quản trị viên');
            setStep(2);
            startTimer(120);
            resetOtpFields();
        } catch (error) {
            const errorMsg = error.response?.data?.message || error.response?.data || 'Không thể khởi tạo tạo bác sĩ';
            setError(errorMsg);
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    const handleResendOtp = async () => {
        if (!canResend) return;

        setLoading(true);
        setError('');
        try {
            const formDataToSend = new FormData();
            formDataToSend.append('userName', formData.userName);
            formDataToSend.append('fullName', formData.fullName);
            formDataToSend.append('email', formData.email);
            formDataToSend.append('phoneNumber', formData.phoneNumber);
            formDataToSend.append('nationalId', formData.nationalId || '');
            if (certificateFile) {
                formDataToSend.append('certificateFile', certificateFile);
            }

            await adminService.initiateCreateDoctor(formDataToSend);
            toast.success('OTP đã được gửi lại đến email quản trị viên');
            startTimer(120);
            resetOtpFields();
        } catch (error) {
            toast.error('Không thể gửi lại OTP');
        } finally {
            setLoading(false);
        }
    };

    const handleConfirm = async (e) => {
        e.preventDefault();
        const otpValue = otp.join('');
        if (otpValue.length !== 6) {
            setError('Vui lòng nhập đầy đủ mã OTP 6 chữ số');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await adminService.confirmCreateDoctor({ otp: otpValue });
            toast.success('Tạo tài khoản bác sĩ thành công! Mật khẩu đã được gửi đến email bác sĩ.');
            if (timerRef.current) clearInterval(timerRef.current);
            navigate('/admin/users');
        } catch (error) {
            let errorMsg = 'OTP không hợp lệ hoặc xác thực thất bại';

            if (error.response?.data) {
                if (typeof error.response.data === 'string') {
                    errorMsg = error.response.data;
                }
                else if (error.response.data.message) {
                    errorMsg = error.response.data.message;
                }
            } else if (error.message) {
                errorMsg = error.message;
            }

            setError(errorMsg);
            toast.error(errorMsg);
            resetOtpFields();
            setLoading(false);
        }
    };

    const handleBack = () => {
        if (timerRef.current) clearInterval(timerRef.current);
        setStep(1);
        setError('');
        setOtp(['', '', '', '', '', '']);
        setTimeLeft(0);
    };

    if (loading) return <LoadingSpinner />;

    return (
        <div>
            <Topbar title="Tạo tài khoản bác sĩ" />
            <div className="p-6">
                <div className="max-w-2xl mx-auto">
                    <div className="flex items-center justify-between mb-8">
                        <div className="flex items-center flex-1">
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 1 ? 'bg-[#100357] text-white' : 'bg-gray-200 text-gray-500'}`}>
                                1
                            </div>
                            <div className={`flex-1 h-1 mx-2 ${step >= 2 ? 'bg-[#100357]' : 'bg-gray-200'}`} />
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 2 ? 'bg-[#100357] text-white' : 'bg-gray-200 text-gray-500'}`}>
                                2
                            </div>
                        </div>
                    </div>

                    {error && (
                        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
                            {error}
                        </div>
                    )}

                    {step === 1 && (
                        <div className="bg-white rounded-lg shadow-sm p-6">
                            <h2 className="text-xl font-semibold text-gray-800 mb-4">Thông tin bác sĩ</h2>
                            <form onSubmit={handleInitiate}>
                                <div className="space-y-4">
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Tên đăng nhập *</label>
                                        <div className="relative">
                                            <FaUser className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                            <input
                                                type="text"
                                                name="userName"
                                                value={formData.userName}
                                                onChange={handleChange}
                                                required
                                                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                                placeholder="bacsi.username"
                                            />
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Họ và tên *</label>
                                        <input
                                            type="text"
                                            name="fullName"
                                            value={formData.fullName}
                                            onChange={handleChange}
                                            required
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                            placeholder="BS. Nguyễn Văn A"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Email *</label>
                                        <div className="relative">
                                            <FaEnvelope className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                            <input
                                                type="email"
                                                name="email"
                                                value={formData.email}
                                                onChange={handleChange}
                                                required
                                                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                                placeholder="bacsi@benhvien.com"
                                            />
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Số điện thoại *</label>
                                        <div className="relative">
                                            <FaPhone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                            <input
                                                type="tel"
                                                name="phoneNumber"
                                                value={formData.phoneNumber}
                                                onChange={handleChange}
                                                required
                                                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                                placeholder="0912345678 hoặc +84912345678"
                                            />
                                        </div>
                                        <p className="text-xs text-gray-400 mt-1">Số điện thoại Việt Nam: 0xxxxxxxxx hoặc +84xxxxxxxxx</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">CCCD/CMND</label>
                                        <div className="relative">
                                            <FaIdCard className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                            <input
                                                type="text"
                                                name="nationalId"
                                                value={formData.nationalId}
                                                onChange={handleChange}
                                                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357]"
                                                placeholder="Nhập CCCD/CMND (9-12 số)"
                                            />
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Bằng cấp / Chứng chỉ</label>
                                        <div className="flex items-center gap-3">
                                            <label className="flex-1 cursor-pointer">
                                                <div className="flex items-center gap-2 px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg hover:border-[#100357] transition">
                                                    <FaUpload className="text-gray-400" />
                                                    <span className="text-sm text-gray-500">
                                                        {certificateFile ? certificateFile.name : 'Chọn file (JPG, PNG, PDF)'}
                                                    </span>
                                                </div>
                                                <input
                                                    id="certificateInput"
                                                    type="file"
                                                    accept=".jpg,.jpeg,.png,.pdf"
                                                    onChange={handleFileChange}
                                                    className="hidden"
                                                />
                                            </label>
                                            {certificateFile && (
                                                <button
                                                    type="button"
                                                    onClick={handleRemoveFile}
                                                    className="text-red-500 hover:text-red-700 text-sm"
                                                >
                                                    Xóa
                                                </button>
                                            )}
                                        </div>
                                        {certificatePreview && (
                                            <div className="mt-2">
                                                <img
                                                    src={certificatePreview}
                                                    alt="Certificate preview"
                                                    className="w-32 h-32 object-cover rounded-lg border border-gray-200"
                                                />
                                            </div>
                                        )}
                                        <p className="text-xs text-gray-400 mt-1">Chấp nhận JPG, PNG, PDF (tối đa 5MB)</p>
                                    </div>
                                </div>
                                <div className="mt-6 flex gap-3">
                                    <button
                                        type="button"
                                        onClick={() => navigate('/admin/users')}
                                        className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                                    >
                                        Hủy
                                    </button>
                                    <button
                                        type="submit"
                                        disabled={loading}
                                        className="flex-1 bg-[#100357] text-white py-2 rounded-lg hover:bg-[#100357]/90 disabled:opacity-50"
                                    >
                                        {loading ? 'Đang gửi OTP...' : 'Tiếp theo →'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}

                    {step === 2 && (
                        <div className="bg-white rounded-lg shadow-sm p-6">
                            <button
                                onClick={handleBack}
                                className="flex items-center gap-2 text-gray-500 hover:text-[#100357] mb-4"
                            >
                                <FaArrowLeft className="w-4 h-4" /> Quay lại
                            </button>
                            <h2 className="text-xl font-semibold text-gray-800 mb-2">Xác thực OTP</h2>
                            <p className="text-gray-500 mb-4">
                                Mã OTP đã được gửi đến email của quản trị viên. Vui lòng nhập mã bên dưới.
                            </p>

                            <form onSubmit={handleConfirm}>
                                <div>
                                    <label className="block text-sm text-gray-600 mb-1">Mã OTP *</label>
                                    <div className="flex gap-2 justify-center mb-4">
                                        {otp.map((digit, index) => (
                                            <input
                                                key={index}
                                                id={`otp-${index}`}
                                                type="text"
                                                maxLength="1"
                                                value={digit}
                                                onChange={(e) => handleOtpChange(index, e.target.value)}
                                                onKeyDown={(e) => handleOtpKeyDown(index, e)}
                                                ref={(el) => otpInputsRef.current[index] = el}
                                                className="w-12 h-12 text-center text-xl font-semibold border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] focus:border-transparent"
                                            />
                                        ))}
                                    </div>

                                    <div className="mb-4">
                                        <div className="flex items-center justify-between mb-1">
                                            <div className="flex items-center gap-2 text-sm">
                                                <FaClock className={`${timeLeft > 30 ? 'text-gray-500' : 'text-red-500 animate-pulse'}`} />
                                                {timeLeft > 0 ? (
                                                    <span className={timeLeft > 30 ? 'text-gray-600' : 'text-red-600 font-semibold'}>
                                                        Còn <span className="font-mono font-bold">{formatTime(timeLeft)}</span>
                                                    </span>
                                                ) : (
                                                    <span className="text-red-500 font-semibold">OTP đã hết hạn</span>
                                                )}
                                            </div>
                                            <button
                                                type="button"
                                                onClick={handleResendOtp}
                                                disabled={!canResend}
                                                className={`text-sm font-medium transition ${
                                                    canResend
                                                        ? 'text-[#100357] hover:underline'
                                                        : 'text-gray-400 cursor-not-allowed'
                                                }`}
                                            >
                                                Gửi lại OTP
                                            </button>
                                        </div>
                                        <div className="w-full h-1.5 bg-gray-200 rounded-full overflow-hidden">
                                            <div
                                                className={`h-full transition-all duration-1000 rounded-full ${
                                                    timeLeft > 30 ? 'bg-[#100357]' : 'bg-red-500'
                                                }`}
                                                style={{ width: `${getProgressPercentage()}%` }}
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="flex gap-3 mt-4">
                                    <button
                                        type="button"
                                        onClick={() => navigate('/admin/users')}
                                        className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                                    >
                                        Hủy
                                    </button>
                                    <button
                                        type="submit"
                                        disabled={loading}
                                        className="flex-1 bg-[#100357] text-white py-2 rounded-lg hover:bg-[#100357]/90 disabled:opacity-50 flex items-center justify-center gap-2"
                                    >
                                        {loading ? 'Đang xác thực...' : <><FaCheck className="w-4 h-4" /> Xác thực & Tạo</>}
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CreateDoctor;