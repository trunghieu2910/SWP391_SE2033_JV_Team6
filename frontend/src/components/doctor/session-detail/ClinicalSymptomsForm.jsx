import React, { useState, useEffect } from 'react';
import { FaSave, FaEdit, FaTimes } from 'react-icons/fa';
import toast from 'react-hot-toast';

// Mapping triệu chứng text -> ID
const symptomIdMap = {
    'RA_MÁU_GIỮA_KỲ': 1,
    'KINH_NGUYỆT_KÉO_DÀI': 2,
    'RONG_KINH': 3,
    'RA_MÁU_SAU_MÃN_KINH': 4,
    'RA_MÁU_SAU_QUAN_HỆ': 5,
    'KHÍ_HƯ_NHIỀU': 6,
    'KHÍ_HƯ_CÓ_MÙI': 7,
    'KHÍ_HƯ_LẪN_MÁU': 8,
    'DỊCH_TIẾT_MÀU_NÂU': 9,
    'ĐAU_VÙNG_CHẬU': 10,
    'ĐAU_BỤNG_DƯỚI': 11,
    'ĐAU_LƯNG_DƯỚI': 12,
    'ĐAU_KHI_QUAN_HỆ': 13,
    'TIỂU_NHIỀU_LẦN': 17,
    'TIỂU_BUỐT': 18,
    'TIỂU_KHÓ': 19,
    'TIỂU_RA_MÁU': 20,
    'TÁO_BÓN': 21,
    'ĐẦY_BỤNG': 22,
    'CHƯỚNG_BỤNG': 23,
    'BUỒN_NÔN': 24
};

// Mapping ID -> text
const idToSymptomMap = Object.fromEntries(
    Object.entries(symptomIdMap).map(([key, value]) => [value, key])
);

const ClinicalSymptomsForm = ({ sessionId, initialData, onSave }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        height: '',
        weight: '',
        menopauseStatus: '',
        abnormalBleeding: [],
        abnormalDischarge: [],
        pain: [],
        systemicSymptoms: {
            weightLoss: false,
            fatigue: false,
            anorexia: false
        },
        urinarySymptoms: [],
        digestiveSymptoms: [],
        riskFactors: {
            familyHistory: false,
            obesity: false,
            diabetes: false,
            hypertension: false,
            pcos: false,
            estrogenTherapy: false
        },
        symptomDuration: '',
        symptomProgressing: false
    });
    const [originalData, setOriginalData] = useState(null);

    const bleedingOptions = [
        {value: 'RA_MÁU_GIỮA_KỲ', label: 'Ra máu giữa kỳ kinh'},
        {value: 'KINH_NGUYỆT_KÉO_DÀI', label: 'Kinh nguyệt kéo dài bất thường'},
        {value: 'RONG_KINH', label: 'Rong kinh (lượng máu nhiều)'},
        {value: 'RA_MÁU_SAU_MÃN_KINH', label: 'Ra máu sau mãn kinh'},
        {value: 'RA_MÁU_SAU_QUAN_HỆ', label: 'Ra máu sau quan hệ'}
    ];

    const dischargeOptions = [
        {value: 'KHÍ_HƯ_NHIỀU', label: 'Khí hư nhiều'},
        {value: 'KHÍ_HƯ_CÓ_MÙI', label: 'Khí hư có mùi hôi'},
        {value: 'KHÍ_HƯ_LẪN_MÁU', label: 'Khí hư lẫn máu'},
        {value: 'DỊCH_TIẾT_MÀU_NÂU', label: 'Dịch tiết màu nâu'}
    ];

    const painOptions = [
        {value: 'ĐAU_VÙNG_CHẬU', label: 'Đau vùng chậu'},
        {value: 'ĐAU_BỤNG_DƯỚI', label: 'Đau bụng dưới'},
        {value: 'ĐAU_LƯNG_DƯỚI', label: 'Đau lưng dưới'},
        {value: 'ĐAU_KHI_QUAN_HỆ', label: 'Đau khi quan hệ'}
    ];

    const urinaryOptions = [
        {value: 'TIỂU_NHIỀU_LẦN', label: 'Tiểu nhiều lần'},
        {value: 'TIỂU_BUỐT', label: 'Tiểu buốt'},
        {value: 'TIỂU_KHÓ', label: 'Tiểu khó'},
        {value: 'TIỂU_RA_MÁU', label: 'Tiểu ra máu'}
    ];

    const digestiveOptions = [
        {value: 'TÁO_BÓN', label: 'Táo bón kéo dài'},
        {value: 'ĐẦY_BỤNG', label: 'Đầy bụng'},
        {value: 'CHƯỚNG_BỤNG', label: 'Chướng bụng'},
        {value: 'BUỒN_NÔN', label: 'Buồn nôn'}
    ];

    const durationOptions = [
        {value: 'DƯỚI_1_THÁNG', label: 'Dưới 1 tháng'},
        {value: '1-3_THÁNG', label: '1–3 tháng'},
        {value: '3-6_THÁNG', label: '3–6 tháng'},
        {value: 'TRÊN_6_THÁNG', label: 'Trên 6 tháng'}
    ];

    useEffect(() => {
        if (initialData) {
            mapInitialDataToForm(initialData);
        }
    }, [initialData]);

    const mapInitialDataToForm = (data) => {
        const symptomDetails = data?.symptomDetails || [];
        const allSymptomIds = symptomDetails.map(sd => sd.symptomId);

        const getTextFromIds = (ids) => {
            return ids.map(id => idToSymptomMap[id]).filter(v => v);
        };

        const abnormalBleedingIds = allSymptomIds.filter(id => id >= 1 && id <= 5);
        const abnormalDischargeIds = allSymptomIds.filter(id => id >= 6 && id <= 9);
        const painIds = allSymptomIds.filter(id => id >= 10 && id <= 13);
        const urinaryIds = allSymptomIds.filter(id => id >= 17 && id <= 20);
        const digestiveIds = allSymptomIds.filter(id => id >= 21 && id <= 24);
        const systemicIds = allSymptomIds.filter(id => id >= 14 && id <= 16);
        const riskFactorIds = allSymptomIds.filter(id => id >= 25 && id <= 30);

        setFormData({
            height: data?.height || '',
            weight: data?.weight || '',
            menopauseStatus: data?.menopauseStatus || '',
            abnormalBleeding: getTextFromIds(abnormalBleedingIds),
            abnormalDischarge: getTextFromIds(abnormalDischargeIds),
            pain: getTextFromIds(painIds),
            systemicSymptoms: {
                weightLoss: systemicIds.includes(14),
                fatigue: systemicIds.includes(15),
                anorexia: systemicIds.includes(16)
            },
            urinarySymptoms: getTextFromIds(urinaryIds),
            digestiveSymptoms: getTextFromIds(digestiveIds),
            riskFactors: {
                familyHistory: riskFactorIds.includes(25),
                obesity: riskFactorIds.includes(26),
                diabetes: riskFactorIds.includes(27),
                hypertension: riskFactorIds.includes(28),
                pcos: riskFactorIds.includes(29),
                estrogenTherapy: riskFactorIds.includes(30)
            },
            symptomDuration: data?.symptomDuration || '',
            symptomProgressing: data?.symptomProgressing || false
        });

        setOriginalData(JSON.parse(JSON.stringify({
            height: data?.height || '',
            weight: data?.weight || '',
            menopauseStatus: data?.menopauseStatus || '',
            abnormalBleeding: getTextFromIds(abnormalBleedingIds),
            abnormalDischarge: getTextFromIds(abnormalDischargeIds),
            pain: getTextFromIds(painIds),
            systemicSymptoms: {
                weightLoss: systemicIds.includes(14),
                fatigue: systemicIds.includes(15),
                anorexia: systemicIds.includes(16)
            },
            urinarySymptoms: getTextFromIds(urinaryIds),
            digestiveSymptoms: getTextFromIds(digestiveIds),
            riskFactors: {
                familyHistory: riskFactorIds.includes(25),
                obesity: riskFactorIds.includes(26),
                diabetes: riskFactorIds.includes(27),
                hypertension: riskFactorIds.includes(28),
                pcos: riskFactorIds.includes(29),
                estrogenTherapy: riskFactorIds.includes(30)
            },
            symptomDuration: data?.symptomDuration || '',
            symptomProgressing: data?.symptomProgressing || false
        })));
    };

    // Component input số tùy chỉnh
    const NumberInput = ({ value, onChange, disabled, placeholder, className }) => {
        const [localValue, setLocalValue] = useState(value || '');

        useEffect(() => {
            setLocalValue(value || '');
        }, [value]);

        const handleChange = (e) => {
            const rawValue = e.target.value;
            if (rawValue === '') {
                setLocalValue('');
                onChange('');
                return;
            }
            if (/^\d*\.?\d*$/.test(rawValue)) {
                setLocalValue(rawValue);
                onChange(rawValue);
            }
        };

        return (
            <input
                type="text"
                value={localValue}
                onChange={handleChange}
                disabled={disabled}
                className={className}
                placeholder={placeholder}
            />
        );
    };

    const handleEdit = () => {
        if (originalData) {
            setFormData(JSON.parse(JSON.stringify(originalData)));
        }
        setIsEditing(true);
    };

    const handleCancel = () => {
        if (originalData) {
            setFormData(JSON.parse(JSON.stringify(originalData)));
        }
        setIsEditing(false);
    };

    const handleCheckboxGroupChange = (field, value, checked) => {
        setFormData(prev => {
            const current = [...prev[field]];
            if (checked) {
                current.push(value);
            } else {
                const index = current.indexOf(value);
                if (index > -1) current.splice(index, 1);
            }
            return {...prev, [field]: current};
        });
    };

    const handleRadioGroupChange = (field, value) => {
        setFormData(prev => ({...prev, [field]: value}));
    };

    const handleSystemicChange = (field, checked) => {
        setFormData(prev => ({
            ...prev,
            systemicSymptoms: {...prev.systemicSymptoms, [field]: checked}
        }));
    };

    const handleRiskFactorChange = (field, checked) => {
        setFormData(prev => ({
            ...prev,
            riskFactors: {...prev.riskFactors, [field]: checked}
        }));
    };

    const getIdsFromValues = (values) => {
        return values.map(v => symptomIdMap[v]).filter(id => id !== undefined);
    };

    const handleSubmit = async () => {
        setLoading(true);
        try {
            const payload = {
                height: formData.height && formData.height !== '' ? parseFloat(formData.height) : null,
                weight: formData.weight && formData.weight !== '' ? parseFloat(formData.weight) : null,
                menopauseStatus: formData.menopauseStatus || null,
                symptomDuration: formData.symptomDuration || null,
                symptomProgressing: formData.symptomProgressing,
                abnormalBleedingIds: getIdsFromValues(formData.abnormalBleeding),
                abnormalDischargeIds: getIdsFromValues(formData.abnormalDischarge),
                painIds: getIdsFromValues(formData.pain),
                urinarySymptomsIds: getIdsFromValues(formData.urinarySymptoms),
                digestiveSymptomsIds: getIdsFromValues(formData.digestiveSymptoms),
                systemicSymptoms: {
                    weightLoss: formData.systemicSymptoms.weightLoss,
                    fatigue: formData.systemicSymptoms.fatigue,
                    anorexia: formData.systemicSymptoms.anorexia
                },
                riskFactors: formData.riskFactors
            };

            console.log('Saving payload:', payload);

            if (onSave) {
                await onSave(payload);
            }

            setOriginalData(JSON.parse(JSON.stringify(formData)));
            toast.success('Đã lưu triệu chứng lâm sàng');
            setIsEditing(false);
        } catch (error) {
            console.error('Save error:', error);
            toast.error(error.response?.data?.message || 'Không thể lưu dữ liệu');
        } finally {
            setLoading(false);
        }
    };

    // Component con để tránh lặp code
    const FormContent = ({disabled}) => (
        <form className="space-y-6">
            {/* Chiều cao, Cân nặng */}
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Chiều cao (cm)</label>
                    <input
                        type="number"
                        defaultValue={formData.height}
                        onBlur={(e) => setFormData(prev => ({ ...prev, height: e.target.value }))}
                        disabled={disabled}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] disabled:bg-gray-100 disabled:text-gray-500"
                        placeholder="Nhập chiều cao"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Cân nặng (kg)</label>
                    <input
                        type="number"
                        defaultValue={formData.weight}
                        onBlur={(e) => setFormData(prev => ({ ...prev, weight: e.target.value }))}
                        disabled={disabled}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#100357] disabled:bg-gray-100 disabled:text-gray-500"
                        placeholder="Nhập cân nặng"
                    />
                </div>
            </div>

            {/* Tình trạng mãn kinh */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Tình trạng mãn kinh</label>
                <div className="flex gap-4">
                    <label className="flex items-center gap-2">
                        <input
                            type="radio"
                            name="menopauseStatus"
                            value="PRE"
                            checked={formData.menopauseStatus === 'PRE'}
                            onChange={(e) => handleRadioGroupChange('menopauseStatus', e.target.value)}
                            disabled={disabled}
                            className="w-4 h-4 disabled:opacity-60"
                        />
                        <span>Chưa mãn kinh</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input
                            type="radio"
                            name="menopauseStatus"
                            value="POST"
                            checked={formData.menopauseStatus === 'POST'}
                            onChange={(e) => handleRadioGroupChange('menopauseStatus', e.target.value)}
                            disabled={disabled}
                            className="w-4 h-4 disabled:opacity-60"
                        />
                        <span>Đã mãn kinh</span>
                    </label>
                </div>
            </div>

            {/* 1. TRIỆU CHỨNG CHÍNH - Ra máu */}
            <div className="border-t pt-4">
                <h4 className="font-semibold text-gray-800 mb-3">1. TRIỆU CHỨNG CHÍNH - RA MÁU ÂM ĐẠO BẤT THƯỜNG</h4>
                <div className="grid grid-cols-2 gap-2">
                    {bleedingOptions.map(opt => (
                        <label key={opt.value} className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={formData.abnormalBleeding.includes(opt.value)}
                                onChange={(e) => handleCheckboxGroupChange('abnormalBleeding', opt.value, e.target.checked)}
                                disabled={disabled}
                                className="w-4 h-4 disabled:opacity-60"
                            />
                            <span className="text-sm">{opt.label}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* 1. TRIỆU CHỨNG CHÍNH - Khí hư */}
            <div>
                <h4 className="font-semibold text-gray-800 mb-3">1. TRIỆU CHỨNG CHÍNH - KHÍ HƯ BẤT THƯỜNG</h4>
                <div className="grid grid-cols-2 gap-2">
                    {dischargeOptions.map(opt => (
                        <label key={opt.value} className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={formData.abnormalDischarge.includes(opt.value)}
                                onChange={(e) => handleCheckboxGroupChange('abnormalDischarge', opt.value, e.target.checked)}
                                disabled={disabled}
                                className="w-4 h-4 disabled:opacity-60"
                            />
                            <span className="text-sm">{opt.label}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* 1. TRIỆU CHỨNG CHÍNH - Đau */}
            <div>
                <h4 className="font-semibold text-gray-800 mb-3">1. TRIỆU CHỨNG CHÍNH - ĐAU</h4>
                <div className="grid grid-cols-2 gap-2">
                    {painOptions.map(opt => (
                        <label key={opt.value} className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={formData.pain.includes(opt.value)}
                                onChange={(e) => handleCheckboxGroupChange('pain', opt.value, e.target.checked)}
                                disabled={disabled}
                                className="w-4 h-4 disabled:opacity-60"
                            />
                            <span className="text-sm">{opt.label}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* 2. TRIỆU CHỨNG TOÀN THÂN */}
            <div className="border-t pt-4">
                <h4 className="font-semibold text-gray-800 mb-3">2. TRIỆU CHỨNG TOÀN THÂN</h4>
                <div className="space-y-2">
                    <label className="flex items-center gap-3">
                        <span className="w-40">Sụt cân không rõ nguyên nhân</span>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="weightLoss"
                                   checked={formData.systemicSymptoms.weightLoss === true}
                                   onChange={() => handleSystemicChange('weightLoss', true)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Có
                        </label>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="weightLoss"
                                   checked={formData.systemicSymptoms.weightLoss === false}
                                   onChange={() => handleSystemicChange('weightLoss', false)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Không
                        </label>
                    </label>
                    <label className="flex items-center gap-3">
                        <span className="w-40">Mệt mỏi kéo dài</span>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="fatigue" checked={formData.systemicSymptoms.fatigue === true}
                                   onChange={() => handleSystemicChange('fatigue', true)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Có
                        </label>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="fatigue" checked={formData.systemicSymptoms.fatigue === false}
                                   onChange={() => handleSystemicChange('fatigue', false)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Không
                        </label>
                    </label>
                    <label className="flex items-center gap-3">
                        <span className="w-40">Chán ăn</span>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="anorexia" checked={formData.systemicSymptoms.anorexia === true}
                                   onChange={() => handleSystemicChange('anorexia', true)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Có
                        </label>
                        <label className="flex items-center gap-2">
                            <input type="radio" name="anorexia" checked={formData.systemicSymptoms.anorexia === false}
                                   onChange={() => handleSystemicChange('anorexia', false)} disabled={disabled}
                                   className="w-4 h-4 disabled:opacity-60"/> Không
                        </label>
                    </label>
                </div>
            </div>

            {/* 3. TRIỆU CHỨNG TIẾT NIỆU */}
            <div>
                <h4 className="font-semibold text-gray-800 mb-3">3. TRIỆU CHỨNG TIẾT NIỆU</h4>
                <div className="grid grid-cols-2 gap-2">
                    {urinaryOptions.map(opt => (
                        <label key={opt.value} className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={formData.urinarySymptoms.includes(opt.value)}
                                onChange={(e) => handleCheckboxGroupChange('urinarySymptoms', opt.value, e.target.checked)}
                                disabled={disabled}
                                className="w-4 h-4 disabled:opacity-60"
                            />
                            <span className="text-sm">{opt.label}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* 4. TRIỆU CHỨNG TIÊU HÓA */}
            <div>
                <h4 className="font-semibold text-gray-800 mb-3">4. TRIỆU CHỨNG TIÊU HÓA</h4>
                <div className="grid grid-cols-2 gap-2">
                    {digestiveOptions.map(opt => (
                        <label key={opt.value} className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={formData.digestiveSymptoms.includes(opt.value)}
                                onChange={(e) => handleCheckboxGroupChange('digestiveSymptoms', opt.value, e.target.checked)}
                                disabled={disabled}
                                className="w-4 h-4 disabled:opacity-60"
                            />
                            <span className="text-sm">{opt.label}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* 5. YẾU TỐ NGUY CƠ */}
            <div className="border-t pt-4">
                <h4 className="font-semibold text-gray-800 mb-3">5. YẾU TỐ NGUY CƠ</h4>
                <div className="grid grid-cols-2 gap-3">
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.familyHistory}
                               onChange={(e) => handleRiskFactorChange('familyHistory', e.target.checked)}
                               disabled={disabled} className="w-4 h-4 disabled:opacity-60"/>
                        <span>Tiền sử gia đình ung thư phụ khoa</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.obesity}
                               onChange={(e) => handleRiskFactorChange('obesity', e.target.checked)} disabled={disabled}
                               className="w-4 h-4 disabled:opacity-60"/>
                        <span>Béo phì</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.diabetes}
                               onChange={(e) => handleRiskFactorChange('diabetes', e.target.checked)}
                               disabled={disabled} className="w-4 h-4 disabled:opacity-60"/>
                        <span>Đái tháo đường</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.hypertension}
                               onChange={(e) => handleRiskFactorChange('hypertension', e.target.checked)}
                               disabled={disabled} className="w-4 h-4 disabled:opacity-60"/>
                        <span>Tăng huyết áp</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.pcos}
                               onChange={(e) => handleRiskFactorChange('pcos', e.target.checked)} disabled={disabled}
                               className="w-4 h-4 disabled:opacity-60"/>
                        <span>Hội chứng buồng trứng đa nang (PCOS)</span>
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="checkbox" checked={formData.riskFactors.estrogenTherapy}
                               onChange={(e) => handleRiskFactorChange('estrogenTherapy', e.target.checked)}
                               disabled={disabled} className="w-4 h-4 disabled:opacity-60"/>
                        <span>Điều trị hormone estrogen kéo dài</span>
                    </label>
                </div>
            </div>

            {/* 6. ĐÁNH GIÁ MỨC ĐỘ TRIỆU CHỨNG */}
            <div className="border-t pt-4">
                <h4 className="font-semibold text-gray-800 mb-3">6. ĐÁNH GIÁ MỨC ĐỘ TRIỆU CHỨNG</h4>
                <div className="space-y-3">
                    <div>
                        <label className="block text-sm text-gray-600 mb-2">Thời gian xuất hiện triệu chứng</label>
                        <div className="flex flex-wrap gap-3">
                            {durationOptions.map(opt => (
                                <label key={opt.value} className="flex items-center gap-2">
                                    <input type="radio" name="symptomDuration" value={opt.value}
                                           checked={formData.symptomDuration === opt.value}
                                           onChange={(e) => handleRadioGroupChange('symptomDuration', e.target.value)}
                                           disabled={disabled} className="w-4 h-4 disabled:opacity-60"/>
                                    <span>{opt.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm text-gray-600 mb-2">Triệu chứng có nặng dần không?</label>
                        <div className="flex gap-4">
                            <label className="flex items-center gap-2">
                                <input type="radio" name="symptomProgressing" value="true"
                                       checked={formData.symptomProgressing === true}
                                       onChange={() => setFormData(prev => ({...prev, symptomProgressing: true}))}
                                       disabled={disabled} className="w-4 h-4 disabled:opacity-60"/> Có
                            </label>
                            <label className="flex items-center gap-2">
                                <input type="radio" name="symptomProgressing" value="false"
                                       checked={formData.symptomProgressing === false}
                                       onChange={() => setFormData(prev => ({...prev, symptomProgressing: false}))}
                                       disabled={disabled} className="w-4 h-4 disabled:opacity-60"/> Không
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    );

    if (!isEditing && originalData && (
        originalData.height || originalData.weight || originalData.menopauseStatus ||
        originalData.abnormalBleeding?.length > 0 || originalData.symptomDuration
    )) {
        const getBleedingLabel = (value) => bleedingOptions.find(o => o.value === value)?.label || value;
        const getDischargeLabel = (value) => dischargeOptions.find(o => o.value === value)?.label || value;
        const getPainLabel = (value) => painOptions.find(o => o.value === value)?.label || value;
        const getDurationLabel = (value) => durationOptions.find(o => o.value === value)?.label || value;
        const getUrinaryLabel = (value) => urinaryOptions.find(o => o.value === value)?.label || value;
        const getDigestiveLabel = (value) => digestiveOptions.find(o => o.value === value)?.label || value;

        return (
            <div className="p-4">
                <div className="flex justify-end mb-4">
                    <button
                        onClick={handleEdit}
                        className="px-3 py-1 text-[#100357] hover:bg-gray-100 rounded-lg transition flex items-center gap-1"
                    >
                        <FaEdit className="w-4 h-4"/> Chỉnh sửa
                    </button>
                </div>

                <div className="space-y-4">
                    {/* Chiều cao, cân nặng - CHỈ HIỂN THỊ TEXT */}
                    {(originalData.height || originalData.weight) && (
                        <div className="grid grid-cols-2 gap-4 pb-2 border-b">
                            {originalData.height && (
                                <div>
                                    <span className="text-gray-500 text-sm">Chiều cao</span>
                                    <p className="font-medium text-gray-800">{originalData.height} cm</p>
                                </div>
                            )}
                            {originalData.weight && (
                                <div>
                                    <span className="text-gray-500 text-sm">Cân nặng</span>
                                    <p className="font-medium text-gray-800">{originalData.weight} kg</p>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Tình trạng mãn kinh */}
                    {originalData.menopauseStatus && (
                        <div>
                            <span className="text-gray-500 text-sm">Tình trạng mãn kinh</span>
                            <div className="mt-1">
                                <span className="inline-block px-3 py-1 bg-gray-100 rounded-full text-sm text-gray-700">
                                    {originalData.menopauseStatus === 'PRE' ? 'Chưa mãn kinh' : 'Đã mãn kinh'}
                                </span>
                            </div>
                        </div>
                    )}

                    {/* Ra máu bất thường */}
                    {originalData.abnormalBleeding?.length > 0 && (
                        <div>
                            <span className="text-gray-500 text-sm">Ra máu bất thường</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.abnormalBleeding.map((item, idx) => (
                                    <span key={idx}
                                          className="inline-block px-3 py-1 bg-red-50 text-red-700 rounded-full text-sm">
                                        {getBleedingLabel(item)}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Khí hư bất thường */}
                    {originalData.abnormalDischarge?.length > 0 && (
                        <div>
                            <span className="text-gray-500 text-sm">Khí hư bất thường</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.abnormalDischarge.map((item, idx) => (
                                    <span key={idx}
                                          className="inline-block px-3 py-1 bg-yellow-50 text-yellow-700 rounded-full text-sm">
                                        {getDischargeLabel(item)}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Đau */}
                    {originalData.pain?.length > 0 && (
                        <div>
                            <span className="text-gray-500 text-sm">Đau</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.pain.map((item, idx) => (
                                    <span key={idx}
                                          className="inline-block px-3 py-1 bg-orange-50 text-orange-700 rounded-full text-sm">
                                        {getPainLabel(item)}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Triệu chứng toàn thân */}
                    {(originalData.systemicSymptoms?.weightLoss ||
                        originalData.systemicSymptoms?.fatigue ||
                        originalData.systemicSymptoms?.anorexia) && (
                        <div>
                            <span className="text-gray-500 text-sm">Triệu chứng toàn thân</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.systemicSymptoms.weightLoss && (
                                    <span className="inline-block px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">Sụt cân không rõ nguyên nhân</span>
                                )}
                                {originalData.systemicSymptoms.fatigue && (
                                    <span className="inline-block px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">Mệt mỏi kéo dài</span>
                                )}
                                {originalData.systemicSymptoms.anorexia && (
                                    <span className="inline-block px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">Chán ăn</span>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Triệu chứng tiết niệu */}
                    {originalData.urinarySymptoms?.length > 0 && (
                        <div>
                            <span className="text-gray-500 text-sm">Triệu chứng tiết niệu</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.urinarySymptoms.map((item, idx) => (
                                    <span key={idx}
                                          className="inline-block px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                                        {getUrinaryLabel(item)}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Triệu chứng tiêu hóa */}
                    {originalData.digestiveSymptoms?.length > 0 && (
                        <div>
                            <span className="text-gray-500 text-sm">Triệu chứng tiêu hóa</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.digestiveSymptoms.map((item, idx) => (
                                    <span key={idx}
                                          className="inline-block px-3 py-1 bg-green-50 text-green-700 rounded-full text-sm">
                                        {getDigestiveLabel(item)}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Yếu tố nguy cơ */}
                    {(originalData.riskFactors?.familyHistory ||
                        originalData.riskFactors?.obesity ||
                        originalData.riskFactors?.diabetes ||
                        originalData.riskFactors?.hypertension ||
                        originalData.riskFactors?.pcos ||
                        originalData.riskFactors?.estrogenTherapy) && (
                        <div>
                            <span className="text-gray-500 text-sm">Yếu tố nguy cơ</span>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {originalData.riskFactors.familyHistory && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Tiền sử gia đình ung thư phụ khoa</span>
                                )}
                                {originalData.riskFactors.obesity && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Béo phì</span>
                                )}
                                {originalData.riskFactors.diabetes && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Đái tháo đường</span>
                                )}
                                {originalData.riskFactors.hypertension && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Tăng huyết áp</span>
                                )}
                                {originalData.riskFactors.pcos && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Hội chứng buồng trứng đa nang</span>
                                )}
                                {originalData.riskFactors.estrogenTherapy && (
                                    <span className="inline-block px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">Điều trị estrogen kéo dài</span>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Thời gian triệu chứng */}
                    {originalData.symptomDuration && (
                        <div>
                            <span className="text-gray-500 text-sm">Thời gian triệu chứng</span>
                            <div className="mt-1">
                                <span className="inline-block px-3 py-1 bg-gray-100 rounded-full text-sm text-gray-700">
                                    {getDurationLabel(originalData.symptomDuration)}
                                </span>
                            </div>
                        </div>
                    )}

                    {/* Diễn biến */}
                    {originalData.symptomProgressing !== undefined && (
                        <div>
                            <span className="text-gray-500 text-sm">Diễn biến</span>
                            <div className="mt-1">
                                <span
                                    className={`inline-block px-3 py-1 rounded-full text-sm ${originalData.symptomProgressing ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'}`}>
                                    {originalData.symptomProgressing ? 'Nặng dần' : 'Không đổi'}
                                </span>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    }

    // Chưa có dữ liệu
    if (!isEditing && !originalData) {
        return (
            <div className="p-4 text-center">
                <p className="text-gray-400 mb-3">Chưa có dữ liệu triệu chứng lâm sàng</p>
                <button
                    onClick={handleEdit}
                    className="px-4 py-2 bg-[#100357] text-white rounded-lg hover:bg-[#100357]/90"
                >
                    <FaEdit className="inline mr-2"/> Nhập triệu chứng
                </button>
            </div>
        );
    }

    // Chế độ chỉnh sửa (Edit mode)
    return (
        <div className="p-4 border-t border-gray-200">
            <FormContent disabled={false}/>

            {/* Nút Lưu và Hủy - ở dưới cùng, chia đều */}
            <div className="flex gap-3 mt-6 pt-4 border-t border-gray-200">
                <button
                    onClick={handleCancel}
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition flex items-center justify-center gap-2"
                >
                    <FaTimes className="w-4 h-4"/> Hủy
                </button>
                <button
                    onClick={handleSubmit}
                    disabled={loading}
                    className="flex-1 bg-[#100357] text-white py-2 rounded-lg hover:bg-[#100357]/90 disabled:opacity-50 flex items-center justify-center gap-2"
                >
                    <FaSave className="w-4 h-4"/> {loading ? 'Đang lưu...' : 'Lưu'}
                </button>
            </div>
        </div>
    );
};

export default ClinicalSymptomsForm;