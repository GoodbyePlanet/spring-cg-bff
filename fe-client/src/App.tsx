import React, { useEffect, useState } from 'react';
import axiosInstance, { getCookie } from './axios/axiosInstance';

const backendBaseUrl = import.meta.env.VITE_AUTH_BFF;

interface RegisteredPasskey {
  name: string;
  createdAt: string;
}

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [userName, setUserName] = useState<string>('');
  const [secureResource, setSecureResource] = useState<string>('');
  const [hasNoPermissionForResource, setHasNoPermissionForResource] = useState<boolean>(false);
  const [passwordLeaked, setPasswordLeaked] = useState<boolean>(false);
  const [showRegisterModal, setShowRegisterModal] = useState<boolean>(false);
  const [newPasskeyName, setNewPasskeyName] = useState<string>('');
  const [registeredPasskeys, setRegisteredPasskeys] = useState<RegisteredPasskey[]>([]);

  useEffect(() => {
    getUserInfo();
  }, []);

  const login = () => {
    window.location.href = "/oauth2/authorization/gateway";
  };

  const getUserInfo = async (): Promise<void> => {
    try {
      const response = await axiosInstance.get('/userinfo');
      if (response.data) {
        const data = response?.data;
        setIsAuthenticated(true);
        setUserName(data?.sub);
        setPasswordLeaked(data?.passwordLeaked);

        await fetchRegisteredPasskeys(data?.sub);
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

  const handleRegisterPasskey = async (passkeyName: string): Promise<void> => {
    try {
      const response = await axiosInstance.post(
        '/registration-begin',
        { username: userName, displayName: userName },
        {
          headers: { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') },
        }
      );

      if (response.data) {
        const publicKey = response.data.publicKey;
        publicKey.challenge = Uint8Array.from(atob(base64urlToBase64(publicKey.challenge)), c => c.charCodeAt(0));
        publicKey.user.id = Uint8Array.from(atob(base64urlToBase64(publicKey.user.id)), c => c.charCodeAt(0));

        const cred = (await navigator.credentials.create({ publicKey })) as PublicKeyCredential;

        const data = {
          id: cred.id,
          rawId: bufferToBase64url(cred.rawId),
          type: cred.type,
          authenticatorAttachment: cred.authenticatorAttachment,
          response: {
            attestationObject: bufferToBase64url((cred.response as AuthenticatorAttestationResponse).attestationObject),
            clientDataJSON: bufferToBase64url((cred.response as AuthenticatorAttestationResponse).clientDataJSON),
          },
        };

        const finishResp = (await axiosInstance.post(`/registration-finish/${passkeyName}`, data, {
          headers: { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') },
        })) as { data: { status: string; username: string } };

        if (finishResp?.data?.status === 'registered') {
          await fetchRegisteredPasskeys(userName);
        }

        setShowRegisterModal(false);
        setNewPasskeyName('');
      }
    } catch (error: any) {
      console.error('Error registering passkey', error);
    }
  };

  const fetchRegisteredPasskeys = async (userName: string) => {
    try {
      const resp = await axiosInstance.get(`/user-passkeys/${userName}`, {
        headers: { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') },
      });

      if (resp.data) {
        setRegisteredPasskeys(_ => [...resp?.data?.userPasskeys?.passkeys]);
      } else {
        setRegisteredPasskeys([]);
      }
    } catch (error) {
      console.error('Error fetching registered passkeys', error);
    }
  };

  const base64urlToBase64 = (base64url: string) => {
    base64url = base64url.replace(/-/g, '+').replace(/_/g, '/');
    const pad = base64url.length % 4;
    return base64url + (pad ? '='.repeat(4 - pad) : '');
  };

  const bufferToBase64url = (buffer: ArrayBuffer) => {
    return btoa(String.fromCharCode(...new Uint8Array(buffer)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
  };

  const xsrfCookie = (): string | undefined => getCookie('XSRF-TOKEN');

  return (
    <>
      <div className="relative min-h-screen w-screen bg-white flex flex-col items-center justify-center">
        {isAuthenticated ? (
          <>
            <form id="logout-form" action="/logout" method="POST">
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
              {passwordLeaked && (
                <span className="text-lg font-bold text-gray-800">
                  Please change your password, it's been found in data breaches!
                </span>
              )}
              {!secureResource && (
                <button
                  onClick={getSecureResource}
                  className="bg-black text-white px-6 py-3 rounded hover:bg-gray-800 transition"
                >
                  Get Secure Resource
                </button>
              )}
              <button
                className="bg-black text-white px-4 py-2 rounded hover:bg-gray-800 transition"
                onClick={() => setShowRegisterModal(true)}
              >
                Register passkey
              </button>
            </div>

            {/* Registered passkeys list */}
            {registeredPasskeys.length > 0 && (
              <div className="text-black flex flex-col items-center mt-3">
                <h3 className="font-bold text-lg mb-2">Registered Passkeys:</h3>
                <ul className="list-disc list-inside">
                  {registeredPasskeys.map((p, i) => (
                    <li
                      key={i}
                      className="flex items-center justify-between bg-gray-100 dark:bg-gray-800 px-4 py-2 rounded-lg shadow-sm hover:bg-gray-200 transition"
                    >
                      <span className="text-xs text-gray-500">Passkey #{i + 1}</span>
                      <span className="font-medium text-gray-900 dark:text-gray-100 pl-2">{p.name}</span>
                      <span className="font-medium text-gray-900 dark:text-gray-100 pl-2">{p.createdAt}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </>
        ) : (
          <button
            onClick={login}
            className="px-4 py-1 border border-gray-800 text-white bg-black rounded hover:bg-gray-200 text-sm"
          >
            Login
          </button>
        )}
        <div className="mt-3 font-bold text-black bg-white flex flex-col items-center justify-center">
          {' '}
          {secureResource && <p>{secureResource}</p>}{' '}
          {hasNoPermissionForResource && (
            <>
              {' '}
              <p>You don't have enough permissions to access secure resource!</p>{' '}
              <button
                onClick={authorizeSecureResource}
                className="mt-3 px-4 py-1 border border-gray-800 text-white bg-black rounded hover:bg-gray-200 text-sm"
              >
                {' '}
                Authorize Secure Resource{' '}
              </button>{' '}
            </>
          )}{' '}
        </div>
      </div>

      {showRegisterModal && (
        <div className="fixed inset-0 bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white text-black border-2 rounded-lg p-6 w-80">
            <h2 className="text-lg font-bold mb-4">Register Passkey</h2>
            <input
              type="text"
              className="w-full border border-gray-300 rounded px-3 py-2 mb-4"
              placeholder="Enter passkey name"
              value={newPasskeyName}
              onChange={e => setNewPasskeyName(e.target.value)}
            />
            <div className="flex justify-end space-x-2">
              <button
                className="px-4 py-2 text-white bg-gray-200 rounded hover:bg-gray-300"
                onClick={() => setShowRegisterModal(false)}
              >
                Cancel
              </button>
              <button
                className="px-4 py-2 bg-black text-white rounded hover:bg-gray-800"
                onClick={() => handleRegisterPasskey(newPasskeyName)}
              >
                Submit
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default App;
