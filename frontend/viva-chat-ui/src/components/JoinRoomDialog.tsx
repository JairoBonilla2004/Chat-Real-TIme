import { useState } from 'react';
import { roomService } from '@/services/roomService';
import { RoomResponse } from '@/types/api';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';

interface JoinRoomDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  room: RoomResponse | null;
  onSuccess: (roomId: number) => void;
}

const JoinRoomDialog = ({ open, onOpenChange, room, onSuccess }: JoinRoomDialogProps) => {
  const { toast } = useToast();
  const [pin, setPin] = useState('');
  const [loading, setLoading] = useState(false);

  const getDeviceId = () => {
    let deviceId = localStorage.getItem('deviceId');
    if (!deviceId) {
      deviceId = `device-${Date.now()}-${Math.random().toString(36).substring(7)}`;
      localStorage.setItem('deviceId', deviceId);
    }
    return deviceId;
  };

  const handleJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!room) return;

    setLoading(true);
    try {
      await roomService.joinRoom({
        roomCode: room.roomCode,
        pin,
        deviceId: getDeviceId(),
      });

      toast({
        title: "Te has unido a la sala",
        description: `Bienvenido a ${room.name}`,
      });

      onSuccess(room.id);
      onOpenChange(false);
      setPin('');
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "PIN incorrecto o sala llena",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Unirse a {room?.name}</DialogTitle>
          <DialogDescription>
            Ingresa el PIN de la sala para continuar
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleJoin} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="pin">PIN</Label>
            <Input
              id="pin"
              type="password"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              placeholder="Ingresa el PIN"
              autoFocus
            />
          </div>
          <div className="flex gap-2 justify-end">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Uniéndose...' : 'Unirse'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default JoinRoomDialog;
