import { create } from 'zustand';
import { client } from '../api/client';

export interface Provider {
    id: string; // UUID
    name: string;
    baseUrl: string;
    authType: string;
    enabled: boolean;
    priority: number;
    rateLimitRequests?: number;
    rateLimitWindowSeconds?: number;
    cloudflareTunnel?: boolean;
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

    createProvider: async (provider: Omit<Provider, 'id'>) => {
        await client.post('/providers', provider);
        // Refresh
        const state = useStore.getState();
        await state.fetchProviders();
    },

    saveCredentials: async (providerId: string, data: Record<string, string>) => {
        await client.post(`/credentials/${providerId}`, data);
    },

    triggerIntake: async (providerName?: string) => {
        await client.post('/data/intake', null, {
            params: { provider: providerName }
        });
    },

    checkHealth: async () => {
        const response = await client.get<Record<string, boolean>>('/health');
        set({ health: response.data });
    }
}));
