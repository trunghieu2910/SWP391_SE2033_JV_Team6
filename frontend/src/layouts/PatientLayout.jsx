import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/patient/Navbar';
import { Toaster } from 'react-hot-toast';

export default function PatientLayout() {
  useEffect(() => {
    document.body.style.backgroundColor = '#f5f8fc';
    return () => {
      document.body.style.backgroundColor = '';
    };
  }, []);

  return (
    <div className="min-h-screen bg-[#f5f8fc] p-4 md:p-6 font-sans">
      <div className="max-w-7xl mx-auto">
        <Navbar />
        <main className="w-full">
          <Outlet />
        </main>
      </div>
      <Toaster position="top-right" />
    </div>
  );
}

