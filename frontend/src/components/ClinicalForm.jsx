import React, { useMemo } from 'react';
import ClinicalSymptomsForm from './doctor/session-detail/ClinicalSymptomsForm';

const ClinicalForm = ({ initialData = {}, onSubmit, loading = false, sessionId = null, readOnly = false }) => {
  const transformedData = useMemo(() => {
    if (!initialData || Object.keys(initialData).length === 0) return null;
    if (initialData.symptomDetails) return initialData;

    return {
      ...initialData,
      symptomDetails: (initialData.symptomIds || []).map(id => ({ symptomId: id }))
    };
  }, [initialData]);

  const handleSave = async (payload) => {
    if (!onSubmit) return;
    return await onSubmit(payload);
  };

  // Nếu đang loading hoặc chưa có dữ liệu
  if (loading) {
    return (
      <div className="p-4 text-center">
        <div className="w-8 h-8 border-4 border-[#100357] border-t-transparent rounded-full animate-spin mx-auto"></div>
        <p className="text-gray-500 mt-2">Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <ClinicalSymptomsForm
      sessionId={sessionId}
      initialData={transformedData}
      onSave={onSubmit ? handleSave : undefined}
      showToast={false}
      readOnly={readOnly || transformedData?.status === 'COMPLETED'}
    />
  );
};

export default ClinicalForm;