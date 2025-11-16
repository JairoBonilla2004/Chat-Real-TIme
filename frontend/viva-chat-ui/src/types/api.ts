// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// Auth Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface GuestLoginRequest {
  nickname: string;
}

export interface UserAdminResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface UserGuestResponse {
  id: number;
  nickname: string;
  expiresAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo?: UserAdminResponse;
  guestInfo?: UserGuestResponse;
}

// Room Types
export enum RoomType {
  TEXT = 'TEXT',
  MULTIMEDIA = 'MULTIMEDIA'
}

export interface CreateRoomRequest {
  name: string;
  description?: string;
  pin: string;
  type: RoomType;
  maxUsers?: number;
  maxFileSizeMb?: number;
}

export interface JoinRoomRequest {
  roomCode: string;
  pin: string;
  deviceId: string;
}

export interface RoomResponse {
  id: number;
  roomCode: string;
  name: string;
  description?: string;
  type: RoomType;
  maxUsers: number;
  currentUsers: number;
  maxFileSizeMb?: number;
  isActive: boolean;
  isFull: boolean;
  createdAt: string;
  plainPin?: string;
  creator?: UserAdminResponse;
}

export interface JoinRoomResponse {
  room: RoomResponse;
  activeSessions: SessionResponse[];
  recentMessages: MessageResponse[];
  activeUsersCount: number;
}

export interface SessionResponse {
  id: number;
  nicknameInRoom: string;
  joinedAt: string;
  isActive: boolean;
  ipAddress: string;
}

// Message Types
export enum MessageType {
  TEXT = 'TEXT',
  FILE = 'FILE'
}

export interface SendMessageRequest {
  roomId: number;
  content: string;
}

export interface MessageResponse {
  id: number;
  content: string;
  messageType: MessageType;
  sentAt: string;
  isEdited: boolean;
  isDeleted?: boolean;
  senderNickname: string;
  senderId: number;
  roomId: number;
  attachments: AttachmentResponse[];
}

export interface AttachmentResponse {
  id: number;
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  fileUrl: string;
}

// WebSocket Types
export interface TypingIndicator {
  username: string;
  isTyping: boolean;
}

export interface UserEvent {
  userId?: number;
  username: string;
  action: 'JOINED' | 'LEFT';
  timestamp?: string;
}

export interface SystemMessage {
  content: string;
  timestamp: string;
}
