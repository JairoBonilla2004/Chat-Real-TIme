import apiClient from '@/config/api';
import {
  ApiResponse,
  MessageResponse,
  SendMessageRequest,
} from '@/types/api';

export const messageService = {
  sendTextMessage: async (messageData: SendMessageRequest): Promise<ApiResponse<MessageResponse>> => {
    const response = await apiClient.post<ApiResponse<MessageResponse>>(
      '/messages/text',
      messageData
    );
    return response.data;
  },

  sendFileMessage: async (
    roomId: number,
    content: string,
    file: File
  ): Promise<ApiResponse<MessageResponse>> => {
    const formData = new FormData();
    formData.append('roomId', roomId.toString());
    formData.append('content', content);
    formData.append('file', file);

    const response = await apiClient.post<ApiResponse<MessageResponse>>(
      '/messages/file',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  getRoomMessages: async (roomId: number): Promise<ApiResponse<MessageResponse[]>> => {
    const response = await apiClient.get<ApiResponse<MessageResponse[]>>(
      `/messages/room/${roomId}`
    );
    return response.data;
  },

  getMessage: async (messageId: number): Promise<ApiResponse<MessageResponse>> => {
    const response = await apiClient.get<ApiResponse<MessageResponse>>(
      `/messages/${messageId}`
    );
    return response.data;
  },

  deleteMessage: async (messageId: number): Promise<ApiResponse<null>> => {
    const response = await apiClient.delete<ApiResponse<null>>(
      `/messages/${messageId}`
    );
    return response.data;
  },
};
