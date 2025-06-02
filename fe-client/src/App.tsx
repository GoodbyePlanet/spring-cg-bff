import React, { useEffect, useState } from 'react';
import axiosInstance from './axios/axiosInstance';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userName, setUserName] = useState('');
  const [messages, setMessages] = useState<string[]>([]);

  useEffect(() => {
    getUserInfo();
    // getMessages();
  }, []);

  const login = () => {
    window.location.href = backendBaseUrl;
  };

  const getUserInfo = async () => {
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

  const getMessages = () => {
    window.location.href = backendBaseUrl + '/resource';
  };

  const logout = async () => {
    try {
      await axiosInstance.post('/logout');
      setIsAuthenticated(false);
      setUserName('');
      setMessages([]);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <>
      <nav className="bg-gray-100 border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
          <button onClick={getMessages} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
            Get Messages
          </button>
          <div className="card">
            {isAuthenticated ? (
              <div className="flex items-center space-x-3">
                <span className="text-sm text-gray-800">{userName}</span>
                <button onClick={logout} className="px-4 py-1 border border-gray-800 rounded hover:bg-gray-200 text-sm">
                  Logout
                </button>
              </div>
            ) : (
              <button onClick={login} className="px-4 py-1 border border-gray-800 rounded hover:bg-gray-200 text-sm">
                Login
              </button>
            )}
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {messages.length > 0 && (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm text-left text-gray-700 border border-gray-200">
              <caption className="text-lg font-semibold mb-2">Messages</caption>
              <thead className="bg-gray-200">
                <tr>
                  <th className="px-4 py-2 border-b">#</th>
                  <th className="px-4 py-2 border-b">Message</th>
                </tr>
              </thead>
              <tbody>
                {messages.map((message, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-4 py-2 border-b">{index + 1}</td>
                    <td className="px-4 py-2 border-b">{message}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
};

export default App;
