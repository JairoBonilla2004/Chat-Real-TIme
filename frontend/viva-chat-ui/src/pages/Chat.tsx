import { useEffect, useState, useRef, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { roomService } from '@/services/roomService';
import { messageService } from '@/services/messageService';
import { useWebSocket } from '@/hooks/useWebSocket';
import { useAuth } from '@/context/AuthContext';
import { MessageResponse, RoomResponse, RoomType } from '@/types/api';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { useToast } from '@/hooks/use-toast';
import { ArrowLeft, Send, Paperclip, Users } from 'lucide-react';
import MessageBubble from '@/components/MessageBubble';
import TypingIndicator from '@/components/TypingIndicator';
import ActiveUsersPanel from '@/components/ActiveUsersPanel';

const Chat = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();

  const [room, setRoom] = useState<RoomResponse | null>(null);
  const [messageInput, setMessageInput] = useState('');
  const [historicalMessages, setHistoricalMessages] = useState<MessageResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout>();

  const [activeUsersById, setActiveUsersById] = useState<Map<number, string>>(new Map());
  const [activeUserNames, setActiveUserNames] = useState<Set<string>>(new Set());

  const { messages: wsMessages, typingUsers, sendMessage, sendTypingIndicator, connected, deletedMessageIds } =
    useWebSocket(roomId ? parseInt(roomId) : null, {
      onUserEvent: (evt) => {
        const displayName = evt.username;
        if (evt.action === 'JOINED') {
          if (typeof evt.userId === 'number') {
            setActiveUsersById((prev) => {
              const next = new Map(prev);
              next.set(evt.userId!, displayName);
              return next;
            });
          }
          setActiveUserNames((prev) => {
            const next = new Set(prev);
            next.add(displayName);
            return next;
          });
        } else if (evt.action === 'LEFT') {
          if (typeof evt.userId === 'number') {
            setActiveUsersById((prev) => {
              const next = new Map(prev);
              next.delete(evt.userId!);
              return next;
            });
          }
          setActiveUserNames((prev) => {
            const next = new Set(prev);
            // Try to remove the mapped display name for this id; fallback to payload name
            if (typeof evt.userId === 'number') {
              const mapped = activeUsersById.get(evt.userId!);
              if (mapped) next.delete(mapped);
            }
            next.delete(displayName);
            return next;
          });
        }
        // Keep room's currentUsers in sync even if activeUsers isn't seeded
        setRoom((prev) => {
          if (!prev) return prev;
          const delta = evt.action === 'JOINED' ? 1 : -1;
          const nextCount = Math.max(0, (prev.currentUsers || 0) + delta);
          return { ...prev, currentUsers: nextCount } as RoomResponse;
        });
        toast({
          title: evt.action === 'JOINED' ? 'Usuario conectado' : 'Usuario salió',
          description: displayName,
        });
      },
      onSystemMessage: () => {},
    });

  const allMessages = useMemo(() => {
    // Merge historical and ws messages by id (ws takes precedence), mark deleted
    const byId = new Map<number, MessageResponse>();
    for (const m of historicalMessages) byId.set(m.id, m);
    for (const m of wsMessages) byId.set(m.id, { ...byId.get(m.id), ...m });
    const merged = Array.from(byId.values()).map((m) =>
      deletedMessageIds.has(m.id) || m.isDeleted
        ? { ...m, isDeleted: true, content: '', attachments: [] }
        : m
    );
    return merged.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime());
  }, [historicalMessages, wsMessages, deletedMessageIds]);

  const connectedNames = useMemo(() => {
    const byId = Array.from(activeUsersById.values());
    const fallbacks = Array.from(activeUserNames).filter((n) => !byId.includes(n));
    return [...byId, ...fallbacks];
  }, [activeUsersById, activeUserNames]);

  useEffect(() => {
    if (roomId) {
      loadRoomData(parseInt(roomId));
    }
  }, [roomId]);

  useEffect(() => {
    scrollToBottom();
  }, [allMessages]);

  const loadRoomData = async (id: number) => {
    setLoading(true);
    try {
      const [roomDetails, messagesResponse] = await Promise.all([
        roomService.getRoomDetails(id),
        messageService.getRoomMessages(id),
      ]);

      setRoom(roomDetails.data.room);
      setHistoricalMessages(messagesResponse.data);
      // Seed active users from current active sessions
      const sessionNames = (roomDetails.data.activeSessions || [])
        .filter((s) => s.isActive)
        .map((s) => s.nicknameInRoom)
        .filter(Boolean);
      setActiveUsersById(new Map());
      setActiveUserNames(new Set(sessionNames));
    } catch (error: any) {
      toast({
        title: "Error",
        description: "No se pudo cargar la sala",
        variant: "destructive",
      });
      navigate('/rooms');
    } finally {
      setLoading(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!messageInput.trim() || !roomId || sending) return;

    setSending(true);
    try {
      sendMessage(messageInput.trim());
      setMessageInput('');
      sendTypingIndicator(false);
    } catch (error) {
      toast({
        title: "Error",
        description: "No se pudo enviar el mensaje",
        variant: "destructive",
      });
    } finally {
      setSending(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setMessageInput(e.target.value);

    // Send typing indicator
    sendTypingIndicator(true);

    // Clear previous timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // Stop typing indicator after 2 seconds of inactivity
    typingTimeoutRef.current = setTimeout(() => {
      sendTypingIndicator(false);
    }, 2000);
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !roomId) return;

    if (room?.type !== RoomType.MULTIMEDIA) {
      toast({
        title: "Error",
        description: "Esta sala no permite envío de archivos",
        variant: "destructive",
      });
      return;
    }

    try {
      // Enviar un contenido por defecto para que el bubble muestre texto inmediatamente
      await messageService.sendFileMessage(parseInt(roomId), 'Archivo adjunto', file);
      toast({
        title: "Archivo enviado",
        description: file.name,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "No se pudo enviar el archivo",
        variant: "destructive",
      });
    }

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleDeleteMessage = async (messageId: number) => {
    try {
      await messageService.deleteMessage(messageId);
      toast({ title: 'Mensaje eliminado' });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'No se pudo eliminar el mensaje',
        variant: 'destructive',
      });
    }
  };

  const handleLeaveRoom = async () => {
    if (!roomId) return;

    try {
      await roomService.leaveRoom(parseInt(roomId));
      navigate('/rooms');
    } catch (error) {
      console.error('Error leaving room:', error);
      navigate('/rooms');
    }
  };

  const getUserName = () => {
    if (!user) return '';
    return 'username' in user ? user.username : user.nickname;
  };

  const getCurrentUserId = () => {
    return user?.id || 0;
  };

  // Derivar lista de usuarios para panel lateral (incluye el propio marcado)
  // IMPORTANTE: Hooks (useMemo) deben ejecutarse antes de cualquier return condicional
  const currentUserId = getCurrentUserId();
  // Unificar lógica con la fila superior: usamos connectedNames como fuente única
  const panelUserEntries = useMemo(() => {
    const selfName = getUserName();
    // Orden alfabético igual que la fila superior
    const sorted = connectedNames.slice().sort((a, b) => a.localeCompare(b));
    return sorted.map((name) => {
      // Buscar id si existe en el mapa
      let foundId: number | undefined;
      for (const [id, display] of activeUsersById.entries()) {
        if (display === name) { foundId = id; break; }
      }
      return { id: foundId, name, isSelf: name === selfName };
    });
  }, [connectedNames, activeUsersById, user]);

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-chat-bg">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-foreground">Cargando sala...</p>
        </div>
      </div>
    );
  }


  return (
    <div className="h-screen flex flex-col bg-chat-bg">
      {/* Header */}
      <header className="bg-sidebar-bg border-b border-border px-4 py-3 flex items-center justify-between sticky top-0 z-10">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={handleLeaveRoom}>
            <ArrowLeft size={20} />
          </Button>
          <div>
            <h1 className="text-lg font-semibold text-foreground">{room?.name}</h1>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Users size={14} />
              <span>
                {(connectedNames.length || room?.currentUsers || 0)} / {room?.maxUsers} usuarios
              </span>
              {connected && <span className="text-primary">• Conectado</span>}
            </div>
          </div>
        </div>
      </header>

      {/* Active users strip */}
      <div className="border-b border-border bg-sidebar-bg px-4 py-2">
        <div className="flex flex-wrap gap-2 items-center">
          <span className="text-xs text-muted-foreground mr-1">Conectados:</span>
          {connectedNames.length === 0 ? (
            <span className="text-xs text-muted-foreground">Sin usuarios activos aún</span>
          ) : (
            connectedNames
              .slice()
              .sort((a, b) => a.localeCompare(b))
              .map((name) => (
                <Badge key={name} variant="secondary" className="text-xs">
                  {name}
                </Badge>
              ))
          )}
        </div>
      </div>

      {/* Main content with sidebar */}
      <div className="flex flex-1 min-h-0">
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4 max-w-4xl mx-auto w-full">
            {allMessages.map((message) => (
              <MessageBubble
                key={message.id}
                message={message}
                isOwn={message.senderId === currentUserId}
                onDelete={message.senderId === currentUserId ? handleDeleteMessage : undefined}
              />
            ))}
            {typingUsers.size > 0 && <TypingIndicator users={Array.from(typingUsers)} />}
            <div ref={messagesEndRef} />
          </div>
        </div>
        {/* Right sidebar (hidden on small screens) */}
        <div className="hidden lg:flex w-64 flex-col border-l border-border bg-background">
          <ActiveUsersPanel users={panelUserEntries} className="flex-1" />
        </div>
      </div>

      {/* Input */}
      <div className="bg-background border-t border-border p-3">
        <form onSubmit={handleSendMessage} className="flex gap-2 max-w-4xl mx-auto">
          <div className="flex w-full items-center gap-2 rounded-xl border bg-card px-2 py-2 shadow-sm">
          {room?.type === RoomType.MULTIMEDIA && (
            <>
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                onChange={handleFileUpload}
              />
              <Button
                type="button"
                variant="outline"
                size="icon"
                onClick={() => fileInputRef.current?.click()}
              >
                <Paperclip size={20} />
              </Button>
            </>
          )}
          <Input
            value={messageInput}
            onChange={handleInputChange}
            placeholder="Escribe un mensaje..."
            className="flex-1"
            disabled={sending || !connected}
          />
          <Button type="submit" disabled={sending || !messageInput.trim() || !connected}>
            <Send size={20} />
          </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Chat;
