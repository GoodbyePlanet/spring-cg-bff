import axios from 'axios';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

const axiosInstance = axios.create({
  baseURL: backendBaseUrl,
  withCredentials: true, // This ensures session cookies are included
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

// Interceptor to prepend base URL only if the request URL is relative,
// For example, "/userinfo" → "http://localhost:8081/userinfo"
axiosInstance.interceptors.request.use(config => {
  if (config.url && !config.url.startsWith('http')) {
    config.url = backendBaseUrl + config.url;
  }
  return config;
});

export function getCookie(name: string) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);

  if (parts.length === 2) {
    // @ts-ignore
    return parts?.pop().split(';').shift();
  }
}

export default axiosInstance;
