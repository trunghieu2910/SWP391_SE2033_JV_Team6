import React from 'react';
import ClinicalSymptomsForm from './doctor/session-detail/ClinicalSymptomsForm';

const ClinicalForm = ({ initialData = {}, readOnly = false, onSubmit, loading = false }) => {
  const transformed = {
    height: initialData.height ?? null,
    weight: initialData.weight ?? null,
    menopauseStatus: initialData.menopauseStatus ?? null,
    symptomDuration: initialData.symptomDuration ?? null,
    symptomProgressing: initialData.symptomProgressing ?? false,
    symptomDetails: (initialData.symptomIds || []).map(id => ({ symptomId: id }))
  };

  // ClinicalSymptomsForm expects an `onSave` that returns a promise.
  const handleSave = async (payload) => {
    if (onSubmit) {
      return await onSubmit(payload);
    }
  };

  return (
    <ClinicalSymptomsForm
      initialData={transformed}
      onSave={onSubmit ? handleSave : undefined}
      allowEdit={!readOnly}
    />
  );
};

export default ClinicalForm;
