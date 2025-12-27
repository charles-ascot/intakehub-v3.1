import { create } from 'zustand';
import { client } from '../api/client';

interface Provider {
    id: string; // UUID
    name: string;
    baseUrl: string;
    authType: string;
    enabled: boolean;
    priority: number;
}

interface AppState {
    providers: Provider[];
    loading: boolean;
    fetchProviders: () => Promise<void>;
    createProvider: (provider: Omit<Provider, 'id'>) => Promise<void>;
    saveCredentials: (providerId: string, data: Record<string, string>) => Promise<void>;
    triggerIntake: (providername?: string) => Promise<void>;
    health: Record<string, boolean>;
    checkHealth: () => Promise<void>;
}

export const useStore = create<AppState>((set) => ({
    providers: [],
    loading: false,
    health: {},

    fetchProviders: async () => {
        set({ loading: true });
        try {
            const response = await client.get<Provider[]>('/providers');
            set({ providers: response.data });
        } finally {
            set({ loading: false });
        }
    },

    createProvider: async (provider) => {
        await client.post('/providers', provider);
        // Refresh
        const state = useStore.getState();
        await state.fetchProviders();
    },

    saveCredentials: async (providerId, data) => {
        await client.post(`/credentials/${providerId}`, data);
    },

    triggerIntake: async (providerName) => {
        await client.post('/data/intake', null, {
            params: { provider: providerName }
        });
    },

    checkHealth: async () => {
        const response = await client.get<Record<string, boolean>>('/health');
        set({ health: response.data });
    }
}));
