import { useEffect, useState, useCallback, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { WS_BASE_URL } from '@/config/api';
import { MessageResponse, MessageType, TypingIndicator, UserEvent } from '@/types/api';
import { messageService } from '@/services/messageService';

interface UseWebSocketReturn {
  messages: MessageResponse[];
  typingUsers: Set<string>;
  sendMessage: (content: string) => void;
  sendTypingIndicator: (isTyping: boolean) => void;
  connected: boolean;
  deletedMessageIds: Set<number>;
}

type UseWsOptions = {
  onUserEvent?: (evt: UserEvent) => void;
  onSystemMessage?: (payload: unknown) => void;
};

export const useWebSocket = (
  roomId: number | null,
  options?: UseWsOptions
): UseWebSocketReturn => {
  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const deletedIdsRef = useRef<Set<number>>(new Set());
  const optionsRef = useRef<UseWsOptions | undefined>(options);

  useEffect(() => {
    optionsRef.current = options;
  }, [options]);

  useEffect(() => {
    if (!roomId) return;

    const token = localStorage.getItem('accessToken');
    if (!token) return;

    const socket = new SockJS(WS_BASE_URL);
    const stompClient = new Client({
      webSocketFactory: () => socket as any,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        console.log('STOMP:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = () => {
      console.log('WebSocket connected');
      setConnected(true);

      // Subscribe to room messages
      stompClient.subscribe(`/topic/room/${roomId}`, async (message) => {
        const incoming: MessageResponse = JSON.parse(message.body);

        // Primer update inmediato para mostrar el mensaje sin lag
        setMessages((prev) => {
          const exists = prev.find((m) => m.id === incoming.id);
          if (exists) return prev.map((m) => (m.id === incoming.id ? { ...exists, ...incoming } : m));
          return [...prev, incoming];
        });

        // Si es un archivo y no trae attachments aún, hidratar desde REST
        if (
          incoming.messageType === MessageType.FILE &&
          (!incoming.attachments || incoming.attachments.length === 0)
        ) {
          try {
            const full = await messageService.getMessage(incoming.id);
            const hydrated = full.data;
            if (hydrated) {
              setMessages((prev) => prev.map((m) => (m.id === hydrated.id ? { ...m, ...hydrated } : m)));
            }
          } catch (e) {
            // Silenciar errores de hidratación; el mensaje seguirá visible con el contenido
            console.warn('No se pudo hidratar attachments del mensaje', incoming.id, e);
          }
        }
      });

      // Subscribe to typing indicators
      stompClient.subscribe(`/topic/room/${roomId}/typing`, (message) => {
        const typingData: TypingIndicator = JSON.parse(message.body);
        setTypingUsers((prev) => {
          const newSet = new Set(prev);
          if (typingData.isTyping) {
            newSet.add(typingData.username);
          } else {
            newSet.delete(typingData.username);
          }
          return newSet;
        });
      });

      // Subscribe to user events
      stompClient.subscribe(`/topic/room/${roomId}/users`, (message) => {
        const userData: UserEvent = JSON.parse(message.body);
        optionsRef.current?.onUserEvent?.(userData);
      });

      // Subscribe to system messages
      stompClient.subscribe(`/topic/room/${roomId}/system`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          optionsRef.current?.onSystemMessage?.(payload);
        } catch {
          optionsRef.current?.onSystemMessage?.(message.body);
        }
      });

      // Subscribe to deleted messages
      stompClient.subscribe(`/topic/room/${roomId}/deleted`, (message) => {
        const deletedMessageId = JSON.parse(message.body).messageId as number;
        deletedIdsRef.current.add(deletedMessageId);
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === deletedMessageId
              ? { ...msg, isDeleted: true, content: '', attachments: [] }
              : msg
          )
        );
      });

      // Notify room of join
      stompClient.publish({
        destination: `/app/chat.joinRoom/${roomId}`,
        body: JSON.stringify({}),
      });
    };

    stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      setConnected(false);
    };

    stompClient.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setConnected(false);
    };

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      if (clientRef.current && roomId) {
        clientRef.current.publish({
          destination: `/app/chat.leaveRoom/${roomId}`,
          body: JSON.stringify({}),
        });
      }
      clientRef.current?.deactivate();
      // reset state for next room
      setMessages([]);
      setTypingUsers(new Set());
      deletedIdsRef.current = new Set();
    };
  }, [roomId]);

  const sendMessage = useCallback(
    (content: string) => {
      if (clientRef.current && clientRef.current.connected && roomId) {
        clientRef.current.publish({
          destination: `/app/chat.sendMessage/${roomId}`,
          body: JSON.stringify({
            roomId,
            content,
          }),
        });
      }
    },
    [roomId]
  );

  const sendTypingIndicator = useCallback(
    (isTyping: boolean) => {
      if (clientRef.current && clientRef.current.connected && roomId) {
        clientRef.current.publish({
          destination: `/app/chat.typing/${roomId}`,
          body: JSON.stringify({ isTyping }),
        });
      }
    },
    [roomId]
  );

  return {
    messages,
    typingUsers,
    sendMessage,
    sendTypingIndicator,
    connected,
    deletedMessageIds: deletedIdsRef.current,
  };
};
