# 🚀 Chat Real-Time API

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue.svg)](https://stomp.github.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Sistema de chat en tiempo real con soporte para múltiples salas, mensajería instantánea, compartir archivos y gestión de usuarios.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Documentación API](#-documentación-api)
  - [Autenticación](#autenticación)
  - [Gestión de Salas](#gestión-de-salas)
  - [Mensajería](#mensajería)
  - [WebSocket](#websocket)
- [Modelos de Datos](#-modelos-de-datos)
- [Ejemplos de Uso](#-ejemplos-de-uso)
- [Despliegue](#-despliegue)
- [Testing](#-testing)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## ✨ Características

### 🔐 Autenticación y Seguridad
- ✅ Sistema de autenticación JWT con refresh tokens
- ✅ Login para administradores y usuarios invitados
- ✅ Cookies HTTP-only para refresh tokens
- ✅ Gestión de sesiones por dispositivo
- ✅ Cierre de sesión múltiple (todos los dispositivos)

### 🏠 Gestión de Salas
- ✅ Creación de salas por administradores
- ✅ Dos tipos de salas: Solo texto y Multimedia
- ✅ Sistema de PIN para acceso seguro
- ✅ Límite configurable de usuarios
- ✅ Códigos únicos de sala generados automáticamente

### 💬 Mensajería en Tiempo Real
- ✅ Chat instantáneo con WebSocket (STOMP)
- ✅ Indicador de "está escribiendo..."
- ✅ Notificaciones de usuarios entrando/saliendo
- ✅ Envío y descarga de archivos (salas multimedia)
- ✅ Eliminación de mensajes propios
- ✅ Historial completo de mensajes

### 👥 Gestión de Usuarios
- ✅ Lista de usuarios conectados en tiempo real
- ✅ Información de sesiones activas
- ✅ Detección de sesiones duplicadas
- ✅ Sesiones temporales para invitados

---

## 🛠 Tecnologías

### Backend
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - Autenticación y autorización
- **Spring WebSocket** - Comunicación en tiempo real
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL** - Base de datos
- **JWT (JSON Web Tokens)** - Tokens de autenticación
- **Lombok** - Reducción de código boilerplate

### Frontend (Recomendado)
- **React** / **Vue** / **Angular**
- **SockJS + STOMP** - Cliente WebSocket
- **Axios** - Cliente HTTP

---

## 📦 Instalación

### Prerrequisitos

```bash
# Java 17 o superior
java -version

# PostgreSQL 12 o superior
psql --version

# Maven 3.6+
mvn -version
```

### Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/chat-real-time-api.git
cd chat-real-time-api
```

### Configurar Base de Datos

```sql
-- Crear base de datos
CREATE DATABASE chatdb;

-- Crear usuario
CREATE USER chatuser WITH PASSWORD 'securepassword';

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE chatdb TO chatuser;
```

### Instalar Dependencias

```bash
mvn clean install
```

### Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

---

## ⚙️ Configuración

### application.properties

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/chatdb
spring.datasource.username=chatuser
spring.datasource.password=securepassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-super-secret-key-change-this-in-production
jwt.expiration=3600000

# File Upload Configuration
file.upload-dir=./uploads
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Guest Session Configuration
app.guest.session-duration-hours=24
app.guest.username-prefix=Guest_

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:4200

# WebSocket Configuration
websocket.allowed-origins=http://localhost:3000,http://localhost:4200
```

### Variables de Entorno (Producción)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/chatdb
export SPRING_DATASOURCE_USERNAME=chatuser
export SPRING_DATASOURCE_PASSWORD=your-secure-password
export JWT_SECRET=your-production-secret-key
export FILE_UPLOAD_DIR=/var/chat/uploads
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

---

## 📚 Documentación API

### Base URL

```
http://localhost:8080/api/v1
```

### WebSocket URL

```
ws://localhost:8080/ws
```

---

## Autenticación

### 1. Login de Administrador

Autentica a un usuario administrador.

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "username": "admin123",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "username": "admin123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "admin@example.com"
    }
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

**Cookies:**
```
refreshToken=xyz123...; HttpOnly; Path=/api/v1/auth/; Max-Age=604800
```

---

### 2. Login de Invitado

Permite acceso temporal sin registro.

**Endpoint:** `POST /api/v1/auth/guest`

**Request:**
```json
{
  "nickname": "Juan"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Acceso de invitado exitoso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "guestInfo": {
      "id": 15,
      "nickname": "Juan#a1b2",
      "expiresAt": "2025-11-13T10:30:00"
    }
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 3. Refrescar Token

Obtiene un nuevo access token.

**Endpoint:** `POST /api/v1/auth/refresh-token`

**Headers:**
```http
Cookie: refreshToken=xyz123...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "username": "admin123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "admin@example.com"
    }
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 4. Cerrar Sesión

Cierra la sesión actual.

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```http
Cookie: refreshToken=xyz123...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null,
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 5. Cerrar Todas las Sesiones

Cierra todas las sesiones del usuario en todos los dispositivos.

**Endpoint:** `POST /api/v1/auth/logout-all`

**Headers:**
```http
Cookie: refreshToken=xyz123...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out from all devices successfully",
  "data": null,
  "timestamp": "2025-11-12T10:30:00"
}
```

---

## Gestión de Salas

### 1. Crear Sala

Crea una nueva sala de chat (solo administradores).

**Endpoint:** `POST /api/v1/rooms/create`

**Authorization:** `ROLE_ADMIN`

**Request:**
```json
{
  "name": "Sala de Desarrollo",
  "description": "Sala para el equipo de desarrollo",
  "pin": "1234",
  "type": "MULTIMEDIA",
  "maxUsers": 50,
  "maxFileSizeMb": 10
}
```

**Validaciones:**
- `name`: Requerido, 3-100 caracteres
- `description`: Opcional, máximo 500 caracteres
- `pin`: Requerido, 4-10 dígitos numéricos
- `type`: `TEXT` o `MULTIMEDIA`
- `maxUsers`: 2-100 (default: 50)
- `maxFileSizeMb`: 1-50 MB (default: 10)

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Sala creada exitosamente",
  "data": {
    "id": 5,
    "roomCode": "ROOMA3F5B1",
    "name": "Sala de Desarrollo",
    "description": "Sala para el equipo de desarrollo",
    "type": "MULTIMEDIA",
    "maxUsers": 50,
    "currentUsers": 0,
    "maxFileSizeMb": 10,
    "isActive": true,
    "isFull": false,
    "createdAt": "2025-11-12T10:30:00",
    "plainPin": "1234",
    "creator": {
      "id": 1,
      "username": "admin123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "admin@example.com"
    }
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

> ⚠️ **IMPORTANTE:** El `plainPin` solo se devuelve en la creación. ¡Guárdalo!

---

### 2. Unirse a una Sala

Permite a un usuario unirse a una sala existente.

**Endpoint:** `POST /api/v1/rooms/join`

**Request:**
```json
{
  "roomCode": "ROOMA3F5B1",
  "pin": "1234",
  "deviceId": "device-fingerprint-abc123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Te has unido a la sala exitosamente",
  "data": {
    "room": {
      "id": 5,
      "roomCode": "ROOMA3F5B1",
      "name": "Sala de Desarrollo",
      "type": "MULTIMEDIA",
      "maxUsers": 50,
      "currentUsers": 3,
      "isActive": true,
      "isFull": false
    },
    "activeSessions": [
      {
        "id": 10,
        "nicknameInRoom": "Juan#a1b2",
        "joinedAt": "2025-11-12T10:25:00",
        "isActive": true,
        "ipAddress": "192.168.1.100"
      }
    ],
    "recentMessages": [],
    "activeUsersCount": 3
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 3. Salir de una Sala

**Endpoint:** `POST /api/v1/rooms/{roomId}/leave`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Has salido de la sala exitosamente",
  "data": null,
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 4. Listar Todas las Salas Activas

**Endpoint:** `GET /api/v1/rooms`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Salas obtenidas exitosamente",
  "data": [
    {
      "id": 5,
      "roomCode": "ROOMA3F5B1",
      "name": "Sala de Desarrollo",
      "description": "Sala para el equipo de desarrollo",
      "type": "MULTIMEDIA",
      "maxUsers": 50,
      "currentUsers": 3,
      "maxFileSizeMb": 10,
      "isActive": true,
      "isFull": false,
      "createdAt": "2025-11-12T10:30:00"
    }
  ],
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 5. Obtener Sala por Código

**Endpoint:** `GET /api/v1/rooms/code/{roomCode}`

---

### 6. Obtener Detalles de Sala

**Endpoint:** `GET /api/v1/rooms/{roomId}/details`

---

### 7. Obtener Mis Salas

**Endpoint:** `GET /api/v1/rooms/my-rooms`

**Authorization:** `ROLE_ADMIN`

---

## Mensajería

### 1. Enviar Mensaje de Texto

**Endpoint:** `POST /api/v1/messages/text`

**Request:**
```json
{
  "roomId": 5,
  "content": "Hola a todos, ¿cómo están?"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Mensaje enviado exitosamente",
  "data": {
    "id": 28,
    "content": "Hola a todos, ¿cómo están?",
    "messageType": "TEXT",
    "sentAt": "2025-11-12T10:45:00",
    "isEdited": false,
    "senderNickname": "Juan#a1b2",
    "senderId": 15,
    "roomId": 5,
    "attachments": []
  },
  "timestamp": "2025-11-12T10:45:00"
}
```

---

### 2. Enviar Archivo

**Endpoint:** `POST /api/v1/messages/file`

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `roomId`: Long (requerido)
- `content`: String (opcional)
- `file`: File (requerido)

**Ejemplo cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/messages/file \
  -H "Authorization: Bearer {token}" \
  -F "roomId=5" \
  -F "content=Documento importante" \
  -F "file=@/path/to/file.pdf"
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Archivo enviado exitosamente",
  "data": {
    "id": 29,
    "content": "Documento importante",
    "messageType": "FILE",
    "sentAt": "2025-11-12T10:50:00",
    "senderNickname": "Juan#a1b2",
    "senderId": 15,
    "roomId": 5,
    "attachments": [
      {
        "id": 12,
        "fileName": "a1b2c3d4-e5f6-7890.pdf",
        "originalFileName": "documento.pdf",
        "fileType": "application/pdf",
        "fileSize": 2048576,
        "fileUrl": "/api/files/a1b2c3d4-e5f6-7890.pdf"
      }
    ]
  },
  "timestamp": "2025-11-12T10:50:00"
}
```

---

### 3. Obtener Mensajes de una Sala

**Endpoint:** `GET /api/v1/messages/room/{roomId}`

---

### 4. Obtener Mensaje por ID

**Endpoint:** `GET /api/v1/messages/{messageId}`

---

### 5. Eliminar Mensaje

**Endpoint:** `DELETE /api/v1/messages/{messageId}`

---

## WebSocket

### Configuración de Conexión

**URL:** `ws://localhost:8080/ws`

**Protocolo:** STOMP sobre WebSocket

### Autenticación

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

const headers = {
  'Authorization': 'Bearer ' + accessToken
};

stompClient.connect(headers, onConnected, onError);
```

### Suscripciones

#### 1. Mensajes de Sala

**Destino:** `/topic/room/{roomId}`

```javascript
stompClient.subscribe('/topic/room/5', function(message) {
  const messageData = JSON.parse(message.body);
  console.log('Nuevo mensaje:', messageData);
});
```

#### 2. Indicador de Escritura

**Destino:** `/topic/room/{roomId}/typing`

```javascript
stompClient.subscribe('/topic/room/5/typing', function(message) {
  const typingData = JSON.parse(message.body);
  if (typingData.isTyping) {
    console.log(typingData.username + ' está escribiendo...');
  }
});
```

#### 3. Eventos de Usuarios

**Destino:** `/topic/room/{roomId}/users`

```javascript
stompClient.subscribe('/topic/room/5/users', function(message) {
  const userData = JSON.parse(message.body);
  if (userData.action === 'JOINED') {
    console.log(userData.username + ' se unió a la sala');
  } else if (userData.action === 'LEFT') {
    console.log(userData.username + ' salió de la sala');
  }
});
```

#### 4. Mensajes del Sistema

**Destino:** `/topic/room/{roomId}/system`

#### 5. Mensajes Eliminados

**Destino:** `/topic/room/{roomId}/deleted`

### Envío de Mensajes

#### 1. Enviar Mensaje de Chat

```javascript
stompClient.send('/app/chat.sendMessage/5', {}, JSON.stringify({
  roomId: 5,
  content: 'Hola desde WebSocket'
}));
```

#### 2. Enviar Indicador de Escritura

```javascript
// Usuario comienza a escribir
stompClient.send('/app/chat.typing/5', {}, JSON.stringify({
  isTyping: true
}));

// Usuario deja de escribir
stompClient.send('/app/chat.typing/5', {}, JSON.stringify({
  isTyping: false
}));
```

#### 3. Notificar Ingreso a Sala

```javascript
stompClient.send('/app/chat.joinRoom/5', {}, JSON.stringify({}));
```

#### 4. Notificar Salida de Sala

```javascript
stompClient.send('/app/chat.leaveRoom/5', {}, JSON.stringify({}));
```

---

## 📊 Modelos de Datos

### RoomType (Enum)
- `TEXT` - Solo mensajes de texto
- `MULTIMEDIA` - Mensajes de texto y archivos

### MessageType (Enum)
- `TEXT` - Mensaje de texto
- `FILE` - Mensaje con archivo adjunto

### UserAdminResponse
```json
{
  "id": 1,
  "username": "admin123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "admin@example.com"
}
```

### UserGuestResponse
```json
{
  "id": 15,
  "nickname": "Juan#a1b2",
  "expiresAt": "2025-11-13T10:30:00"
}
```

### AttachmentResponse
```json
{
  "id": 12,
  "fileName": "a1b2c3d4-e5f6-7890.pdf",
  "originalFileName": "documento.pdf",
  "fileType": "application/pdf",
  "fileSize": 2048576,
  "fileUrl": "/api/files/a1b2c3d4-e5f6-7890.pdf"
}
```

---

## 💡 Ejemplos de Uso

### Ejemplo Completo: Cliente JavaScript

```javascript
// 1. Login
const loginResponse = await fetch('http://localhost:8080/api/v1/auth/guest', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ nickname: 'Juan' })
});
const { data } = await loginResponse.json();
const accessToken = data.accessToken;

// 2. Conectar WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { 'Authorization': `Bearer ${accessToken}` },
  function onConnected() {
    console.log('WebSocket conectado');
    
    // 3. Suscribirse a mensajes de sala
    stompClient.subscribe('/topic/room/5', function(message) {
      const msg = JSON.parse(message.body);
      console.log('Mensaje recibido:', msg.content);
    });
    
    // 4. Enviar mensaje
    stompClient.send('/app/chat.sendMessage/5', {}, JSON.stringify({
      roomId: 5,
      content: 'Hola desde JavaScript!'
    }));
  },
  function onError(error) {
    console.error('Error WebSocket:', error);
  }
);
```

### Ejemplo: React Hook para WebSocket

```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export function useWebSocket(roomId, accessToken) {
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    if (!roomId || !accessToken) return;

    const socket = new SockJS('http://localhost:8080/ws');
    const client = Stomp.over(socket);

    client.connect(
      { 'Authorization': `Bearer ${accessToken}` },
      () => {
        client.subscribe(`/topic/room/${roomId}`, (message) => {
          const newMessage = JSON.parse(message.body);
          setMessages(prev => [...prev, newMessage]);
        });

        client.send(`/app/chat.joinRoom/${roomId}`, {}, JSON.stringify({}));
      }
    );

    setStompClient(client);

    return () => {
      if (client) {
        client.send(`/app/chat.leaveRoom/${roomId}`, {}, JSON.stringify({}));
        client.disconnect();
      }
    };
  }, [roomId, accessToken]);

  const sendMessage = (content) => {
    if (stompClient && stompClient.connected) {
      stompClient.send(`/app/chat.sendMessage/${roomId}`, {}, JSON.stringify({
        roomId,
        content
      }));
    }
  };

  return { messages, sendMessage };
}
```

---

## 🚀 Despliegue

### Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: chatdb
      POSTGRES_USER: chatuser
      POSTGRES_PASSWORD: securepassword
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/chatdb
      SPRING_DATASOURCE_USERNAME: chatuser
      SPRING_DATASOURCE_PASSWORD: securepassword
      JWT_SECRET: your-production-secret-key
    depends_on:
      - postgres
    volumes:
      - ./uploads:/var/chat/uploads

volumes:
  postgres_data:
```

### Dockerfile

```dockerfile
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Ejecutar con Docker

```bash
# Construir y ejecutar
docker-compose up -d

# Ver logs
docker-compose logs -f backend

# Detener
docker-compose down
```

---

## 🧪 Testing

### Tests Unitarios

```bash
mvn test
```

### Tests de Integración

```bash
mvn verify
```

### Colección Postman

Importa la colección incluida en `/postman/ChatAPI.postman_collection.json`

### Casos de Prueba Principales

1. **Autenticación**
   - ✅ Login exitoso (admin y guest)
   - ✅ Login fallido (credenciales incorrectas)
   - ✅ Refresh token válido
   - ✅ Logout

2. **Salas**
   - ✅ Crear sala (admin)
   - ✅ Unirse con PIN correcto
   - ✅ Unirse con PIN incorrecto (debe fallar)
   - ✅ Sala llena (debe fallar)
   - ✅ Salir de sala

3. **Mensajes**
   - ✅ Enviar mensaje texto
   - ✅ Enviar archivo en sala multimedia
   - ✅ Enviar archivo en sala texto (debe fallar)
   - ✅ Eliminar mensaje propio
   - ✅ Eliminar mensaje ajeno (debe fallar)

4. **WebSocket**
   - ✅ Conexión con token válido
   - ✅ Recibir mensajes en tiempo real
   - ✅ Indicador de escritura
   - ✅ Notificaciones join/leave

---

## ⚠️ Códigos de Error

| Código | Descripción | Ejemplo |
|--------|-------------|---------|
| 400 | Bad Request | Datos inválidos, validaciones fallidas |
| 401 | Unauthorized | Token inválido o expirado |
| 403 | Forbidden | Sin permisos para la acción |
| 404 | Not Found | Recurso no encontrado |
| 409 | Conflict | Sala llena, sesión duplicada |
| 423 | Locked | Cuenta bloqueada |
| 500 | Internal Server Error | Error interno del servidor |

### Estructura de Error

```json
{
  "success": false,
  "message": "Descripción del error",
  "data": null,
  "timestamp": "2025-11-12T11:30:00"
}
```

---

## 🔒 Seguridad

### Checklist de Seguridad

- ✅ Tokens JWT con expiración corta
- ✅ Refresh tokens con rotación
- ✅ Cookies HTTP-only
- ✅ CORS configurado correctamente
- ✅ Validación de inputs
- ✅ Sanitización de mensajes (prevención XSS)
- ✅ Rate limiting
- ✅ Autenticación en WebSocket
- ✅ Validación de tamaño y tipo de archivos
- ✅ Gestión de sesiones por dispositivo

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas!

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add: AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Guía de Estilo

- Usa Java Code Conventions
- Escribe tests para nuevas funcionalidades
- Actualiza la documentación
- Usa commits descriptivos (Conventional Commits)

---

## 📝 Roadmap

### Fase 1 (MVP) ✅
- [x] Login admin y guest
- [x] Crear/unirse a salas
- [x] Chat en tiempo real
- [x] Upload de archivos
- [x] Gestión de sesiones

### Fase 2 (En Desarrollo)
- [ ] Editar mensajes
- [ ] Reacciones (emojis)
- [ ] Responder a mensajes (threads)
- [ ] Buscar en mensajes