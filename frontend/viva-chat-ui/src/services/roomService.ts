import apiClient from '@/config/api';
import {
  ApiResponse,
  RoomResponse,
  CreateRoomRequest,
  JoinRoomRequest,
  JoinRoomResponse,
} from '@/types/api';

export const roomService = {
  createRoom: async (roomData: CreateRoomRequest): Promise<ApiResponse<RoomResponse>> => {
    const response = await apiClient.post<ApiResponse<RoomResponse>>(
      '/rooms/create',
      roomData
    );
    return response.data;
  },

  joinRoom: async (joinData: JoinRoomRequest): Promise<ApiResponse<JoinRoomResponse>> => {
    const response = await apiClient.post<ApiResponse<JoinRoomResponse>>(
      '/rooms/join',
      joinData
    );
    return response.data;
  },

  leaveRoom: async (roomId: number): Promise<ApiResponse<null>> => {
    const response = await apiClient.post<ApiResponse<null>>(
      `/rooms/${roomId}/leave`
    );
    return response.data;
  },

  getAllRooms: async (): Promise<ApiResponse<RoomResponse[]>> => {
    const response = await apiClient.get<ApiResponse<RoomResponse[]>>('/rooms');
    return response.data;
  },

  getRoomByCode: async (roomCode: string): Promise<ApiResponse<RoomResponse>> => {
    const response = await apiClient.get<ApiResponse<RoomResponse>>(
      `/rooms/code/${roomCode}`
    );
    return response.data;
  },

  getRoomDetails: async (roomId: number): Promise<ApiResponse<JoinRoomResponse>> => {
    const response = await apiClient.get<ApiResponse<JoinRoomResponse>>(
      `/rooms/${roomId}/details`
    );
    return response.data;
  },

  getMyRooms: async (): Promise<ApiResponse<RoomResponse[]>> => {
    const response = await apiClient.get<ApiResponse<RoomResponse[]>>('/rooms/my-rooms');
    return response.data;
  },

  resetRoomPin: async (roomId: number): Promise<ApiResponse<RoomResponse>> => {
    const response = await apiClient.post<ApiResponse<RoomResponse>>(`/rooms/${roomId}/reset-pin`);
    return response.data;
  },
};
