import React, { useEffect, useState } from 'react';
import axiosInstance from './axios/axiosInstance';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userName, setUserName] = useState('');
  const [resource, setResource] = useState<string>('');

  useEffect(() => {
    getUserInfo();
  }, []);

  const login = () => {
    window.location.href = backendBaseUrl;
  };

  const getUserInfo = async (): Promise<void> => {
    try {
      const response = await axiosInstance.get('/userinfo');
      if (response.data) {
        setIsAuthenticated(true);
        setUserName(response.data.sub);
      }
    } catch (error) {
      console.error('Error getting user info', error);
    }
  };

  const getSecureResource = async (): Promise<void> => {
    try {
      const response = await axiosInstance.get('/resource');
      if (response.data) {
        console.log('RESOURCE', response.data);
        setResource(response.data);
      }
    } catch (error) {
      console.error('Error getting resource data', error);
    }
  };

  const logout = async (): Promise<void> => {
    try {
      await axiosInstance.post('/logout');
      setIsAuthenticated(false);
      setUserName('');
      setResource('');
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <>
      <div className="relative min-h-screen w-screen bg-white flex flex-col items-center justify-center">
        {isAuthenticated ? (
          <>
            <button
              onClick={logout}
              className="absolute top-4 right-4 bg-black text-white px-4 py-2 rounded hover:bg-gray-800 transition"
            >
              Logout
            </button>
            <div className="flex flex-col items-center space-y-4">
              <span className="text-sm text-gray-800">{userName}</span>
              {!resource && (
                <button
                  onClick={getSecureResource}
                  className="bg-black text-white px-6 py-3 rounded hover:bg-gray-800 transition"
                >
                  Get Secure Resource
                </button>
              )}
            </div>
          </>
        ) : (
          <button onClick={login} className="px-4 py-1 border border-gray-800 rounded hover:bg-gray-200 text-sm">
            Login
          </button>
        )}
        <div className="mt-3 font-bold text-black bg-white flex items-center justify-center">
          {resource && <p>{resource}</p>}
        </div>
      </div>
    </>
  );
};

export default App;
