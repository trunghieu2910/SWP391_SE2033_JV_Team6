import React from 'react';

const Topbar = ({ title }) => {
    return (
        <div className="px-6 py-4 border-b border-gray-100">
            <h1 className="text-2xl font-bold text-gray-800">{title}</h1>
        </div>
    );
};

export default Topbar;