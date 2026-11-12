
import axios from 'axios';

const api = axios.create({
    baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Attach the JWT to requests when a token is available.
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');

        // Don't attach an old JWT to login or signup requests.
        const isAuthRequest = config.url?.startsWith('/auth/');

        if (token && !isAuthRequest) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
