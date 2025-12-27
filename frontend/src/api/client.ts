import axios from 'axios';
import type { AxiosResponse, AxiosError } from 'axios';

// In production, this would come from env vars
const API_URL = 'http://localhost:8080/api/v1';

export const client = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add interceptor for JWT if implemented on frontend login
// For now, we assume public access or basic implementation
client.interceptors.response.use(
    (response: AxiosResponse) => response,
    (error: AxiosError) => {
        console.error('API Error:', error.response?.data || error.message);
        return Promise.reject(error);
    }
);
