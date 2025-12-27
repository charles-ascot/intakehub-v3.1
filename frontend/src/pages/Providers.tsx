import React, { useEffect, useState } from 'react';
import { useStore } from '../store/useStore';
import type { Provider } from '../store/useStore';

export const Providers: React.FC = () => {
    const { providers, fetchProviders, createProvider, saveCredentials } = useStore();
    const [isAdding, setIsAdding] = useState(false);
    const [selectedProvider, setSelectedProvider] = useState<string | null>(null);
    const [formData, setFormData] = useState<any>({});

    useEffect(() => {
        fetchProviders();
    }, []);

    const handleCreate = async () => {
        await createProvider({
            name: formData.name,
            baseUrl: formData.baseUrl,
            authType: 'API_KEY_HEADER', // Simplified
            enabled: true,
            priority: 1,
            rateLimitRequests: 100,
            rateLimitWindowSeconds: 60,
            cloudflareTunnel: false
        });
        setIsAdding(false);
    };

    const handleCreds = async () => {
        if (!selectedProvider) return;
        // Parse JSON or use key-value pairs
        // Simple key-value text area
        try {
            const data = JSON.parse(formData.credsJson || '{}');
            await saveCredentials(selectedProvider, data);
            alert('Credentials Saved');
            setSelectedProvider(null);
        } catch (e) {
            alert('Invalid JSON');
        }
    };

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">Providers Management</h1>

            <button onClick={() => setIsAdding(!isAdding)} className="mb-4 bg-gray-200 px-3 py-1 rounded">
                {isAdding ? 'Cancel' : 'Add Provider'}
            </button>

            {isAdding && (
                <div className="bg-white p-4 mb-4 border rounded">
                    <input placeholder="Name" className="block border p-2 mb-2 w-full" onChange={e => setFormData({ ...formData, name: e.target.value })} />
                    <input placeholder="Base URL" className="block border p-2 mb-2 w-full" onChange={e => setFormData({ ...formData, baseUrl: e.target.value })} />
                    <button onClick={handleCreate} className="bg-green-600 text-white px-4 py-2 rounded">Save</button>
                </div>
            )}

            {selectedProvider && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                    <div className="bg-white p-6 rounded w-96">
                        <h3 className="font-bold mb-2">Update Credentials</h3>
                        <p className="text-xs text-gray-500 mb-2">Enter credentials as JSON (e.g. {'{"username": "foo"}'})</p>
                        <textarea
                            className="w-full border p-2 h-32"
                            onChange={e => setFormData({ ...formData, credsJson: e.target.value })}
                        ></textarea>
                        <div className="mt-4 flex justify-end gap-2">
                            <button onClick={() => setSelectedProvider(null)} className="px-3 py-1 bg-gray-200 rounded">Cancel</button>
                            <button onClick={handleCreds} className="px-3 py-1 bg-blue-600 text-white rounded">Encrypt & Save</button>
                        </div>
                    </div>
                </div>
            )}

            <div className="overflow-x-auto">
                <table className="min-w-full bg-white border">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="p-3 text-left">Name</th>
                            <th className="p-3 text-left">Type</th>
                            <th className="p-3 text-left">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {providers.map((p: Provider) => (
                            <tr key={p.id} className="border-t">
                                <td className="p-3">{p.name}</td>
                                <td className="p-3">{p.authType}</td>
                                <td className="p-3">
                                    <button onClick={() => setSelectedProvider(p.id)} className="text-blue-600 hover:underline">
                                        Manage Credentials
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
