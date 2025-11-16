import { useState } from 'react';
import { roomService } from '@/services/roomService';
import { RoomType } from '@/types/api';
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
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';

interface CreateRoomDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const CreateRoomDialog = ({ open, onOpenChange, onSuccess }: CreateRoomDialogProps) => {
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    pin: '',
    type: RoomType.TEXT,
    maxUsers: 50,
    maxFileSizeMb: 10,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await roomService.createRoom(formData);
      toast({
        title: "Sala creada exitosamente",
        description: `Código de sala: ${response.data.roomCode}. PIN: ${response.data.plainPin}`,
      });
      onSuccess();
      onOpenChange(false);
      setFormData({
        name: '',
        description: '',
        pin: '',
        type: RoomType.TEXT,
        maxUsers: 50,
        maxFileSizeMb: 10,
      });
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "No se pudo crear la sala",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Crear Nueva Sala</DialogTitle>
          <DialogDescription>
            Crea una sala de chat para tu equipo
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Nombre de la Sala *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Ej: Sala de Desarrollo"
              required
              minLength={3}
              maxLength={100}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Descripción</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Descripción opcional de la sala"
              maxLength={500}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="pin">PIN de la Sala *</Label>
            <Input
              id="pin"
              value={formData.pin}
              onChange={(e) => setFormData({ ...formData, pin: e.target.value })}
              placeholder="El PIN se genera automáticamente al crear la sala"
              required
              minLength={4}
              maxLength={10}
              disabled
            />
          </div>
         

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="type">Tipo de Sala</Label>
              <Select
                value={formData.type}
                onValueChange={(value) => setFormData({ ...formData, type: value as RoomType })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={RoomType.TEXT}>Solo Texto</SelectItem>
                  <SelectItem value={RoomType.MULTIMEDIA}>Multimedia</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="maxUsers">Máximo de Usuarios</Label>
              <Input
                id="maxUsers"
                type="number"
                value={formData.maxUsers}
                onChange={(e) => setFormData({ ...formData, maxUsers: parseInt(e.target.value) })}
                min={2}
                max={100}
              />
            </div>
          </div>

          {formData.type === RoomType.MULTIMEDIA && (
            <div className="space-y-2">
              <Label htmlFor="maxFileSizeMb">Tamaño Máximo de Archivo (MB)</Label>
              <Input
                id="maxFileSizeMb"
                type="number"
                value={formData.maxFileSizeMb}
                onChange={(e) => setFormData({ ...formData, maxFileSizeMb: parseInt(e.target.value) })}
                min={1}
                max={50}
              />
            </div>
          )}

          <div className="flex gap-2 justify-end pt-4">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Creando...' : 'Crear Sala'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CreateRoomDialog;
