# Instrucciones para Docker Compose

## Ejecutar la aplicación

### Opción 1: Construcción y ejecución completa
```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# En modo detached (en segundo plano)
docker-compose up --build -d
```

### Opción 2: Ejecutar servicios específicos
```bash
# Solo la base de datos
docker-compose up mysql-chat

# Backend y base de datos
docker-compose up mysql-chat backend

# Todos los servicios
docker-compose up
```

## Comandos útiles

### Ver logs
```bash
# Logs de todos los servicios
docker-compose logs

# Logs de un servicio específico
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql-chat

# Seguir logs en tiempo real
docker-compose logs -f
```

### Detener servicios
```bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (¡CUIDADO! Esto eliminará los datos de la BD)
docker-compose down -v
```

### Reconstruir servicios
```bash
# Reconstruir solo un servicio
docker-compose build backend
docker-compose build frontend

# Reconstruir todos los servicios
docker-compose build
```

### Acceso a contenedores
```bash
# Acceder al contenedor del backend
docker-compose exec backend bash

# Acceder al contenedor de MySQL
docker-compose exec mysql-chat mysql -u jairo -p chat_real
```

## URLs de acceso

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Base de datos MySQL**: localhost:3306

## Credenciales de la base de datos

- **Host**: mysql-chat (dentro de Docker) o localhost (desde el host)
- **Puerto**: 3306
- **Base de datos**: chat_real
- **Usuario**: jairo
- **Contraseña**: jairo123
- **Usuario root**: root
- **Contraseña root**: root123

## Troubleshooting

### Si hay problemas con la base de datos:
```bash
# Verificar que el contenedor esté corriendo
docker-compose ps

# Ver logs del contenedor MySQL
docker-compose logs mysql-chat

# Reiniciar solo MySQL
docker-compose restart mysql-chat
```

### Si hay problemas con la construcción:
```bash
# Limpiar caché de Docker
docker system prune

# Reconstruir sin caché
docker-compose build --no-cache
```

### Si hay problemas de permisos:
```bash
# Asegurar permisos correctos
sudo chown -R $USER:$USER .
```