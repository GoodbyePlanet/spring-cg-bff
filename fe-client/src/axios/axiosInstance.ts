import axios from 'axios';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

const axiosInstance = axios.create({
  baseURL: backendBaseUrl,
  withCredentials: true, // This ensures session cookies are included
});

// Interceptor to prepend base URL only if the request URL is relative
axiosInstance.interceptors.request.use(config => {
  console.log('HEERE>>>>', config);
  if (config.url && !config.url.startsWith('http')) {
    console.log('base url', backendBaseUrl + config.url);
    config.url = backendBaseUrl + config.url;
  }
  return config;
});

export default axiosInstance;
