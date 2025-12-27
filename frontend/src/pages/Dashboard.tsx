import React, { useEffect } from 'react';
import { useStore } from '../store/useStore';

export const Dashboard: React.FC = () => {
    const { checkHealth, health, triggerIntake } = useStore();

    useEffect(() => {
        checkHealth();
        const interval = setInterval(checkHealth, 30000); // 30s poll
        return () => clearInterval(interval);
    }, []);

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">System Health Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                {Object.entries(health).map(([provider, isHealthy]) => (
                    <div key={provider} className={`p-4 rounded shadow ${isHealthy ? 'bg-green-100 border-green-500' : 'bg-red-100 border-red-500'} border`}>
                        <h3 className="font-bold text-lg">{provider}</h3>
                        <p className="text-sm">{isHealthy ? 'OPERATIONAL' : 'DOWN'}</p>
                    </div>
                ))}
            </div>

            <div className="bg-white p-6 rounded shadow">
                <h2 className="text-xl font-bold mb-4">Actions</h2>
                <button
                    onClick={() => triggerIntake()}
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                >
                    Trigger Global Intake
                </button>
            </div>
        </div>
    );
};
