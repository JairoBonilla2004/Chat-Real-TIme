import apiClient from '@/config/api';
import {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  GuestLoginRequest,
} from '@/types/api';

export const authService = {
  login: async (credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      '/auth/login',
      credentials
    );
    return response.data;
  },

  guestLogin: async (guestData: GuestLoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      '/auth/guest',
      guestData
    );
    return response.data;
  },

  refreshToken: async (): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      '/auth/refresh-token'
    );
    return response.data;
  },

  logout: async (): Promise<ApiResponse<null>> => {
    const response = await apiClient.post<ApiResponse<null>>('/auth/logout');
    return response.data;
  },

  logoutAll: async (): Promise<ApiResponse<null>> => {
    const response = await apiClient.post<ApiResponse<null>>('/auth/logout-all');
    return response.data;
  },

  registerAdmin: async (payload: {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
  }): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register-admin', payload);
    return response.data;
  },
};
