import React from 'react';

const StatsCard = ({ title, value, icon: Icon, color = '#097300', trend, trendValue }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-4 border border-gray-100 hover:shadow-md transition">
            <div className="flex items-center justify-between mb-2">
                <div
                    className="p-2 rounded-lg text-white"
                    style={{ backgroundColor: color }}
                >
                    <Icon className="w-5 h-5" />
                </div>
                <span className="text-2xl font-bold text-gray-800">
          {value?.toLocaleString() || 0}
        </span>
            </div>
            <p className="text-gray-500 text-sm">{title}</p>
            {trend && (
                <p className={`text-xs mt-1 ${trend === 'up' ? 'text-green-600' : 'text-red-600'}`}>
                    {trendValue}
                </p>
            )}
        </div>
    );
};

export default StatsCard;