import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { roomService } from '@/services/roomService';
import { RoomResponse, RoomType } from '@/types/api';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { Plus, Users, Lock, MessageSquare, LogOut, Eye, KeyRound, Clipboard } from 'lucide-react';
import CreateRoomDialog from '@/components/CreateRoomDialog';
import JoinRoomDialog from '@/components/JoinRoomDialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const Rooms = () => {
  const navigate = useNavigate();
  const { user, isAdmin, logout } = useAuth();
  const { toast } = useToast();

  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [joinDialogOpen, setJoinDialogOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<RoomResponse | null>(null);

  // Guest join inline form state
  const [roomCodeInput, setRoomCodeInput] = useState('');
  const [pinInput, setPinInput] = useState('');
  const [joining, setJoining] = useState(false);

  useEffect(() => {
    loadRooms();
  }, []);

  const loadRooms = async () => {
    setLoading(true);
    try {
      if (isAdmin) {
        const response = await roomService.getMyRooms();
        setRooms(response.data);
      } else {
        setRooms([]); // guests no ven lista
      }
    } catch (error: any) {
      toast({
        title: "Error",
        description: "No se pudieron cargar las salas",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleJoinRoom = (room: RoomResponse) => {
    setSelectedRoom(room);
    setJoinDialogOpen(true);
  };

  const handleGuestJoinInline = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roomCodeInput.trim() || !pinInput.trim()) {
      toast({ title: 'Faltan datos', description: 'Ingresa código de sala y PIN', variant: 'destructive' });
      return;
    }
    setJoining(true);
    try {
      const resp = await roomService.joinRoom({ roomCode: roomCodeInput.trim(), pin: pinInput.trim(), deviceId: '' });
      const roomId = resp.data.room.id;
      navigate(`/chat/${roomId}`);
    } catch (err: any) {
      toast({ title: 'Error', description: err.response?.data?.message || 'No se pudo unir a la sala', variant: 'destructive' });
    } finally {
      setJoining(false);
    }
  };

  const handleResetPin = async (roomId: number) => {
    try {
      const resp = await roomService.resetRoomPin(roomId);
      const newPin = resp.data.plainPin;
      if (newPin) {
        navigator.clipboard?.writeText(newPin).catch(() => {});
        toast({ title: 'PIN reseteado', description: `Nuevo PIN: ${newPin} (copiado al portapapeles)` });
      } else {
        toast({ title: 'PIN reseteado', description: 'Se generó un nuevo PIN' });
      }
      // refresh list in case needed
      loadRooms();
    } catch (err: any) {
      toast({ title: 'Error', description: err.response?.data?.message || 'No se pudo resetear el PIN', variant: 'destructive' });
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const getUserName = () => {
    if (!user) return '';
    return 'username' in user ? user.username : user.nickname;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-primary/5">
      {/* Header */}
      <header className="border-b bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-primary flex items-center justify-center">
              <MessageSquare className="text-primary-foreground" size={24} />
            </div>
            <div>
              <h1 className="text-2xl font-bold">Chat Real-Time</h1>
              <p className="text-sm text-muted-foreground">
                {isAdmin ? 'Administrador' : 'Invitado'}: {getUserName()}
              </p>
            </div>
          </div>
          <div className="flex gap-2">
            {isAdmin && (
              <Button onClick={() => setCreateDialogOpen(true)}>
                <Plus size={16} className="mr-2" />
                Crear Sala
              </Button>
            )}
            <Button variant="outline" onClick={handleLogout}>
              <LogOut size={16} className="mr-2" />
              Salir
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        {isAdmin ? (
          <div className="mb-6">
            <h2 className="text-3xl font-bold mb-2">Mis Salas</h2>
            <p className="text-muted-foreground">Gestiona las salas que has creado</p>
          </div>
        ) : (
          <div className="mb-6">
            <h2 className="text-3xl font-bold mb-2">Unirse a una Sala</h2>
            <p className="text-muted-foreground">Ingresa el código de la sala y el PIN</p>
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <Card key={i} className="animate-pulse">
                <CardHeader>
                  <div className="h-6 bg-muted rounded w-3/4"></div>
                  <div className="h-4 bg-muted rounded w-1/2"></div>
                </CardHeader>
                <CardContent>
                  <div className="h-20 bg-muted rounded"></div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          isAdmin ? (
            rooms.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <MessageSquare size={48} className="text-muted-foreground mb-4" />
                  <p className="text-xl font-semibold mb-2">Aún no has creado salas</p>
                  <p className="text-muted-foreground">Crea tu primera sala para comenzar</p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {rooms.map((room) => (
                  <Card key={room.id} className="hover:shadow-lg transition-shadow">
                    <CardHeader>
                      <div className="flex items-start justify-between gap-2">
                        <CardTitle className="text-xl">{room.name}</CardTitle>
                        <Badge variant={room.type === RoomType.MULTIMEDIA ? 'default' : 'secondary'}>
                          {room.type === RoomType.MULTIMEDIA ? 'Multimedia' : 'Texto'}
                        </Badge>
                      </div>
                      <CardDescription>
                        Código: <span className="font-mono">{room.roomCode}</span>
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-2 text-muted-foreground">
                          <Users size={16} />
                          <span>
                            {room.currentUsers} / {room.maxUsers}
                          </span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Lock size={16} className="text-muted-foreground" />
                          <span className="text-muted-foreground">PIN requerido</span>
                        </div>
                      </div>
                      {room.plainPin && (
                        <div className="flex items-center justify-between bg-muted px-3 py-2 rounded">
                          <span className="text-sm">PIN: <span className="font-mono">{room.plainPin}</span></span>
                          <button
                            className="text-xs inline-flex items-center gap-1 opacity-80 hover:opacity-100"
                            onClick={() => navigator.clipboard?.writeText(room.plainPin || '')}
                            title="Copiar PIN"
                          >
                            <Clipboard size={14} /> Copiar
                          </button>
                        </div>
                      )}
                      <div className="flex gap-2">
                        <Button variant="outline" className="flex-1" onClick={() => handleJoinRoom(room)}>
                          <Eye size={16} className="mr-2" /> Ver detalles
                        </Button>
                        <Button variant="secondary" className="flex-1" onClick={() => handleResetPin(room.id)}>
                          <KeyRound size={16} className="mr-2" /> Resetear PIN
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )
          ) : (
            <div className="max-w-lg mx-auto">
              <Card>
                <CardHeader>
                  <CardTitle>Unirse a una sala</CardTitle>
                  <CardDescription>Ingresa el código de la sala y el PIN que te compartieron</CardDescription>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleGuestJoinInline} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="roomCode">Código de sala</Label>
                      <Input id="roomCode" value={roomCodeInput} onChange={(e) => setRoomCodeInput(e.target.value)} placeholder="ROOMABC123" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="pin">PIN</Label>
                      <Input id="pin" value={pinInput} onChange={(e) => setPinInput(e.target.value)} placeholder="1234" />
                    </div>
                    <Button type="submit" className="w-full" disabled={joining}>
                      {joining ? 'Entrando...' : 'Unirse'}
                    </Button>
                  </form>
                </CardContent>
              </Card>
            </div>
          )
        )}
      </main>

      {/* Dialogs */}
      {isAdmin && (
        <CreateRoomDialog
          open={createDialogOpen}
          onOpenChange={setCreateDialogOpen}
          onSuccess={loadRooms}
        />
      )}
      {isAdmin && (
        <JoinRoomDialog
          open={joinDialogOpen}
          onOpenChange={setJoinDialogOpen}
          room={selectedRoom}
          onSuccess={(roomId) => navigate(`/chat/${roomId}`)}
        />
      )}
    </div>
  );
};

export default Rooms;
