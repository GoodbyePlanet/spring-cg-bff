import React, { useEffect, useState } from 'react';
import axiosInstance, { getCookie } from './axios/axiosInstance';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [userName, setUserName] = useState<string>('');
  const [secureResource, setSecureResource] = useState<string>('');
  const [hasNoPermissionForResource, setHasNoPermissionForResource] = useState<boolean>(false);

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
        setUserName(response?.data?.sub);
      }
    } catch (error) {
      console.error('Error getting user info', error);
    }
  };

  const getSecureResource = async (): Promise<void> => {
    try {
      const response = await axiosInstance.get('/resource');
      if (response.data) {
        setSecureResource(response.data);
        setHasNoPermissionForResource(false);
      }
    } catch (error: any) {
      console.error('Error getting resource data', error);
      if (error?.status === 403) {
        setHasNoPermissionForResource(true);
      }
    }
  };

  const authorizeSecureResource = async (): Promise<void> => {
    window.location.href = backendBaseUrl + '/oauth2/authorization/gateway';
  };

  const xsrfCookie = (): string | undefined => getCookie('XSRF-TOKEN');

  return (
    <>
      <div className="relative min-h-screen w-screen bg-white flex flex-col items-center justify-center">
        {isAuthenticated ? (
          <>
            <form id="logout-form" action="http://localhost:8081/logout" method="POST">
              <input type="hidden" name="_csrf" value={xsrfCookie()} />
              <button
                type="submit"
                className="absolute top-4 right-4 bg-black text-white px-4 py-2 rounded hover:bg-gray-800 transition"
              >
                Logout
              </button>
            </form>
            <div className="flex flex-col items-center space-y-4">
              <span className="text-lg font-bold text-gray-800">Username: {userName?.toUpperCase()}</span>
              {!secureResource && (
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
        <div className="mt-3 font-bold text-black bg-white flex flex-col items-center justify-center">
          {secureResource && <p>{secureResource}</p>}
          {hasNoPermissionForResource && (
            <>
              <p>You don't have enough permissions to access secure resource!</p>
              <button
                onClick={authorizeSecureResource}
                className="mt-3 px-4 py-1 border border-gray-800 text-white rounded hover:bg-gray-200 text-sm"
              >
                Authorize Secure Resource
              </button>
            </>
          )}
        </div>
      </div>
    </>
  );
};

export default App;
