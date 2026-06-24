import React from 'react';
import { User, Calendar, Phone, Mail, CreditCard, MapPin } from 'lucide-react';

export default function PatientProfile({ profile }) {
  if (!profile) return null;

  const fullName = profile.fullName || 'Phạm Thùy Linh';
  const patientID = profile.patientID || '1';
  const username = profile.username || 'patient_nam';
  
  // Format Date of Birth
  const formatDob = (dobString) => {
    if (!dobString) return '15/8/1995';
    try {
      const date = new Date(dobString);
      return date.toLocaleDateString('vi-VN');
    } catch (e) {
      return dobString;
    }
  };

  // Format gender
  const formatGender = (g) => {
    if (!g) return 'Nam';
    if (g.toLowerCase() === 'nu' || g.toLowerCase() === 'female') return 'Nữ';
    return 'Nam';
  };

  const infoList = [
    {
      icon: <User className="w-5 h-5 text-blue-500" />,
      label: 'Tên tài khoản',
      value: username,
    },
    {
      icon: <Calendar className="w-5 h-5 text-blue-500" />,
      label: 'Ngày sinh',
      value: formatDob(profile.dob),
    },
    {
      icon: <User className="w-5 h-5 text-blue-500" />, // Standard User icon represents Gender
      label: 'Giới tính',
      value: formatGender(profile.gender),
    },
    {
      icon: <Phone className="w-5 h-5 text-blue-500" />,
      label: 'Số điện thoại',
      value: profile.phoneNumber || '0904445466',
    },
    {
      icon: <Mail className="w-5 h-5 text-blue-500" />,
      label: 'Email',
      value: profile.email || 'namvipnhatg@gmail.com',
    },
    {
      icon: <CreditCard className="w-5 h-5 text-blue-500" />,
      label: 'CCCD/CMND',
      value: profile.nationalID || '043678901234',
    },
    {
      icon: <MapPin className="w-5 h-5 text-blue-500" />,
      label: 'Địa chỉ',
      value: profile.address || 'Cầu Giấy, Hà Nội',
    },
  ];

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center">
      {/* Large circular avatar with letter 'P' */}
      <div className="w-24 h-24 bg-gradient-to-tr from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white text-4xl font-bold shadow-md shadow-blue-100 mb-4 hover:scale-105 transition-transform duration-200">
        {fullName.charAt(0).toUpperCase() || 'P'}
      </div>

      {/* Patient Name */}
      <h2 className="text-xl font-bold text-gray-800 tracking-tight">{fullName}</h2>
      
      {/* Patient ID */}
      <span className="text-xs font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-full mt-2 uppercase tracking-wider">
        MÃ BỆNH NHÂN: #{patientID}
      </span>

      {/* Info List */}
      <div className="w-full mt-6 space-y-4">
        {infoList.map((item, index) => (
          <div
            key={index}
            className={`flex items-center justify-between pb-3 ${
              index === infoList.length - 1 ? '' : 'border-b border-gray-50'
            }`}
          >
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-50/50 rounded-lg text-blue-600">
                {item.icon}
              </div>
              <span className="text-sm font-semibold text-gray-500">{item.label}</span>
            </div>
            <span className="text-sm font-bold text-gray-800 text-right max-w-[200px] truncate" title={item.value}>
              {item.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
