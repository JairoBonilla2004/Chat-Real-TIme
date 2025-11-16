import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '@/services/authService';
import { UserAdminResponse, UserGuestResponse } from '@/types/api';

interface AuthContextType {
  user: UserAdminResponse | UserGuestResponse | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isGuest: boolean;
  login: (username: string, password: string) => Promise<void>;
  guestLogin: (nickname: string) => Promise<void>;
  logout: () => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserAdminResponse | UserGuestResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('accessToken');

    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await authService.login({ username, password });
      const { accessToken, userInfo } = response.data;

      localStorage.setItem('accessToken', accessToken);
      if (userInfo) {
        localStorage.setItem('user', JSON.stringify(userInfo));
        setUser(userInfo);
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const guestLogin = async (nickname: string) => {
    try {
      const response = await authService.guestLogin({ nickname });
      const { accessToken, guestInfo } = response.data;

      localStorage.setItem('accessToken', accessToken);
      if (guestInfo) {
        localStorage.setItem('user', JSON.stringify(guestInfo));
        setUser(guestInfo);
      }
    } catch (error) {
      console.error('Guest login failed:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  const isAuthenticated = !!user;
  const isAdmin = user ? 'username' in user : false;
  const isGuest = user ? 'nickname' in user : false;

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isAdmin,
        isGuest,
        login,
        guestLogin,
        logout,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
