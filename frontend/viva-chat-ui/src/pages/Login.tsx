import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useToast } from '@/hooks/use-toast';
import { MessageSquare } from 'lucide-react';

const Login = () => {
  const navigate = useNavigate();
  const { login, guestLogin } = useAuth();
  const { toast } = useToast();

  const [adminCredentials, setAdminCredentials] = useState({ username: '', password: '' });
  const [guestNickname, setGuestNickname] = useState('');
  const [loading, setLoading] = useState(false);
  const [registerData, setRegisterData] = useState({
    username: '', password: '', firstName: '', lastName: '', email: '', phone: ''
  });

  const handleAdminLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await login(adminCredentials.username, adminCredentials.password);
      toast({
        title: "Inicio de sesión exitoso",
        description: "Bienvenido de vuelta",
      });
      navigate('/rooms');
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Credenciales inválidas",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleGuestLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!guestNickname.trim()) {
      toast({
        title: "Error",
        description: "Por favor ingresa un nickname",
        variant: "destructive",
      });
      return;
    }

    setLoading(true);
    try {
      await guestLogin(guestNickname);
      toast({
        title: "Acceso de invitado exitoso",
        description: `Bienvenido, ${guestNickname}`,
      });
      navigate('/rooms');
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "No se pudo crear sesión de invitado",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleAdminRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    const { username, password, firstName, lastName, email } = registerData;
    if (!username || !password || !firstName || !lastName || !email) {
      toast({ title: 'Datos incompletos', description: 'Completa todos los campos obligatorios', variant: 'destructive' });
      return;
    }
    setLoading(true);
    try {
      const resp = await (await import('@/services/authService')).authService.registerAdmin(registerData);
      const { accessToken } = resp.data;
      localStorage.setItem('accessToken', accessToken);
      toast({ title: 'Administrador registrado', description: 'Sesión iniciada automáticamente' });
      navigate('/rooms');
    } catch (error: any) {
      toast({ title: 'Error', description: error.response?.data?.message || 'No se pudo registrar', variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/20 via-background to-secondary/20 p-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary text-primary-foreground mb-2">
            <MessageSquare size={32} />
          </div>
          <h1 className="text-4xl font-bold tracking-tight">Chat Real-Time</h1>
          <p className="text-muted-foreground">Conecta con tu equipo al instante</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Iniciar Sesión</CardTitle>
            <CardDescription>Inicia sesión como administrador o invitado</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="guest" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="guest">Invitado</TabsTrigger>
                <TabsTrigger value="admin">Administrador</TabsTrigger>
                <TabsTrigger value="register">Registrarse</TabsTrigger>
              </TabsList>

              <TabsContent value="guest">
                <form onSubmit={handleGuestLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="nickname">Nickname</Label>
                    <Input
                      id="nickname"
                      placeholder="Tu nombre..."
                      value={guestNickname}
                      onChange={(e) => setGuestNickname(e.target.value)}
                      disabled={loading}
                    />
                  </div>
                  <Button type="submit" className="w-full" disabled={loading}>
                    {loading ? 'Conectando...' : 'Entrar como Invitado'}
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="admin">
                <form onSubmit={handleAdminLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="username">Usuario</Label>
                    <Input
                      id="username"
                      placeholder="admin123"
                      value={adminCredentials.username}
                      onChange={(e) =>
                        setAdminCredentials({ ...adminCredentials, username: e.target.value })
                      }
                      disabled={loading}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="password">Contraseña</Label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="••••••••"
                      value={adminCredentials.password}
                      onChange={(e) =>
                        setAdminCredentials({ ...adminCredentials, password: e.target.value })
                      }
                      disabled={loading}
                    />
                  </div>
                  <Button type="submit" className="w-full" disabled={loading}>
                    {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="register">
                <form onSubmit={handleAdminRegister} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div className="space-y-2">
                      <Label htmlFor="firstName">Nombres</Label>
                      <Input id="firstName" value={registerData.firstName} onChange={(e) => setRegisterData({ ...registerData, firstName: e.target.value })} />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="lastName">Apellidos</Label>
                      <Input id="lastName" value={registerData.lastName} onChange={(e) => setRegisterData({ ...registerData, lastName: e.target.value })} />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input id="email" type="email" value={registerData.email} onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="phone">Teléfono (opcional)</Label>
                    <Input id="phone" value={registerData.phone} onChange={(e) => setRegisterData({ ...registerData, phone: e.target.value })} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="regUsername">Usuario</Label>
                    <Input id="regUsername" value={registerData.username} onChange={(e) => setRegisterData({ ...registerData, username: e.target.value })} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="regPassword">Contraseña</Label>
                    <Input id="regPassword" type="password" value={registerData.password} onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })} />
                  </div>
                  <Button type="submit" className="w-full" disabled={loading}>
                    {loading ? 'Creando...' : 'Crear cuenta administrador'}
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Login;
