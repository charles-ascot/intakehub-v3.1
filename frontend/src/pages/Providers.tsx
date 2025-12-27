import React, { useEffect, useState } from 'react';
import { useStore } from '../store/useStore';
import type { Provider } from '../store/useStore';

export const Providers: React.FC = () => {
    const { providers, fetchProviders, createProvider, saveCredentials } = useStore();
    const [isAdding, setIsAdding] = useState(false);
    const [selectedProvider, setSelectedProvider] = useState<string | null>(null);

    // Form States
    const [formData, setFormData] = useState<Partial<Omit<Provider, 'id'>>>({
        name: '',
        baseUrl: '',
        authType: 'API_KEY_HEADER',
        enabled: true,
        priority: 1,
        rateLimitRequests: 100,
        rateLimitWindowSeconds: 60
    });

    const [credsData, setCredsData] = useState<{ username?: string, password?: string, cert?: string, key?: string }>({});

    useEffect(() => {
        fetchProviders();
    }, []);

    const handleCreate = async () => {
        if (!formData.name || !formData.baseUrl) {
            alert("Name and Base URL are required");
            return;
        }

        try {
            await createProvider(formData as Omit<Provider, 'id'>);
            setIsAdding(false);
            setFormData({
                name: '',
                baseUrl: '',
                authType: 'API_KEY_HEADER',
                enabled: true,
                priority: 1,
                rateLimitRequests: 100,
                rateLimitWindowSeconds: 60
            });
            alert('Provider created successfully');
        } catch (e) {
            console.error(e);
            alert('Failed to create provider');
        }
    };

    const handleSaveCreds = async () => {
        if (!selectedProvider) return;
        try {
            // Convert to Record<string, string>
            const dataToSave: Record<string, string> = {};
            if (credsData.username) dataToSave.username = credsData.username;
            if (credsData.password) dataToSave.password = credsData.password;
            if (credsData.cert) dataToSave.cert_pem = credsData.cert;
            if (credsData.key) dataToSave.key_pem = credsData.key;

            await saveCredentials(selectedProvider, dataToSave);
            alert('Credentials Saved');
            setSelectedProvider(null);
            setCredsData({});
        } catch (e) {
            console.error(e);
            alert('Failed to save credentials');
        }
    };

    return (
        <div className="p-6 max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold mb-6 text-gray-800">Providers Management</h1>

            <button
                onClick={() => setIsAdding(!isAdding)}
                className={`mb-6 px-4 py-2 rounded-lg font-medium transition-colors ${isAdding ? 'bg-red-100 text-red-700 hover:bg-red-200' : 'bg-blue-600 text-white hover:bg-blue-700'
                    }`}
            >
                {isAdding ? 'Cancel Adding' : '+ Add New Provider'}
            </button>

            {isAdding && (
                <div className="bg-white p-6 mb-6 border border-gray-200 rounded-xl shadow-sm space-y-4">
                    <h2 className="text-xl font-semibold mb-4">New Provider Details</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                            <input
                                className="w-full border rounded-lg p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                value={formData.name}
                                onChange={e => setFormData({ ...formData, name: e.target.value })}
                                placeholder="e.g. Betfair Exchange"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Base URL</label>
                            <input
                                className="w-full border rounded-lg p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                value={formData.baseUrl}
                                onChange={e => setFormData({ ...formData, baseUrl: e.target.value })}
                                placeholder="https://api.example.com"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Auth Type</label>
                            <select
                                className="w-full border rounded-lg p-2 bg-white"
                                value={formData.authType}
                                onChange={e => setFormData({ ...formData, authType: e.target.value })}
                            >
                                <option value="API_KEY_HEADER">API Key (Header)</option>
                                <option value="BEARER_TOKEN">Bearer Token</option>
                                <option value="BASIC_AUTH">Basic Auth</option>
                                <option value="CERTIFICATE">Certificate (mTLS)</option>
                                <option value="NONE">None</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
                            <input
                                type="number"
                                className="w-full border rounded-lg p-2"
                                value={formData.priority}
                                onChange={e => setFormData({ ...formData, priority: parseInt(e.target.value) })}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Rate Limit (Req/Window)</label>
                            <div className="flex gap-2">
                                <input
                                    type="number"
                                    placeholder="Reqs"
                                    className="w-1/2 border rounded-lg p-2"
                                    value={formData.rateLimitRequests}
                                    onChange={e => setFormData({ ...formData, rateLimitRequests: parseInt(e.target.value) })}
                                />
                                <input
                                    type="number"
                                    placeholder="Secs"
                                    className="w-1/2 border rounded-lg p-2"
                                    value={formData.rateLimitWindowSeconds}
                                    onChange={e => setFormData({ ...formData, rateLimitWindowSeconds: parseInt(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="flex justify-end pt-4">
                        <button onClick={handleCreate} className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 font-medium shadow-sm">
                            Save Provider
                        </button>
                    </div>
                </div>
            )}

            {selectedProvider && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-xl w-full max-w-2xl shadow-2xl">
                        <h3 className="text-2xl font-bold mb-6 text-gray-900 border-b pb-2">Update Credentials</h3>

                        <div className="space-y-4 mb-6">
                            <div className="bg-blue-50 p-4 rounded-lg text-sm text-blue-800 mb-4">
                                <p>Enter sensitive credentials here. They will be encrypted before storage.</p>
                                <p className="mt-1 font-semibold">For Betfair (mTLS), ensure you paste the full content of the generated .crt and .key files.</p>
                            </div>

                            <div className="grid grid-cols-1 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Username / API Key Name</label>
                                    <input
                                        className="w-full border p-2 rounded"
                                        placeholder="Username"
                                        value={credsData.username || ''}
                                        onChange={e => setCredsData({ ...credsData, username: e.target.value })}
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Password / API Key Secret</label>
                                    <input
                                        type="password"
                                        className="w-full border p-2 rounded"
                                        placeholder="Secret"
                                        value={credsData.password || ''}
                                        onChange={e => setCredsData({ ...credsData, password: e.target.value })}
                                    />
                                </div>

                                <div className="border-t pt-4 mt-2">
                                    <h4 className="font-semibold text-gray-700 mb-3">mTLS Certificates (Optional)</h4>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <label className="block text-xs uppercase text-gray-500 mb-1">Client Certificate (PEM)</label>
                                            <textarea
                                                className="w-full border p-2 rounded text-xs font-mono h-24"
                                                placeholder="-----BEGIN CERTIFICATE-----..."
                                                value={credsData.cert || ''}
                                                onChange={e => setCredsData({ ...credsData, cert: e.target.value })}
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-xs uppercase text-gray-500 mb-1">Private Key (PEM)</label>
                                            <textarea
                                                className="w-full border p-2 rounded text-xs font-mono h-24"
                                                placeholder="-----BEGIN PRIVATE KEY-----..."
                                                value={credsData.key || ''}
                                                onChange={e => setCredsData({ ...credsData, key: e.target.value })}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="flex justify-end gap-3 pt-4 border-t">
                            <button onClick={() => { setSelectedProvider(null); setCredsData({}); }} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg">
                                Cancel
                            </button>
                            <button onClick={handleSaveCreds} className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium shadow-sm">
                                Encrypt & Save Credentials
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
                <table className="min-w-full">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Auth Type</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Priority</th>
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {providers.map((p: Provider) => (
                            <tr key={p.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{p.name}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500 text-sm">{p.baseUrl}</td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                                        {p.authType}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500 text-sm">{p.priority}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                    <button
                                        onClick={() => setSelectedProvider(p.id)}
                                        className="text-indigo-600 hover:text-indigo-900 mr-4"
                                    >
                                        Credentials
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {providers.length === 0 && (
                            <tr>
                                <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                                    No providers configured. Click "Add New Provider" to start.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
