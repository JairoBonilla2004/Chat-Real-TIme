# Sistema de Chat en Tiempo Real con Salas Seguras

Este proyecto implementa un sistema de chat en tiempo real con salas seguras, desarrollado como parte del Proyecto Integrador del Parcial I de la materia **Aplicaciones Distribuidas**. El sistema permite comunicación instantánea mediante **WebSockets**, salas protegidas con **PIN**, gestión de archivos en salas multimedia y un flujo de interacción administrado por un backend robusto en **Spring Boot**.

---

Este proyecto fue elaborado por Alejandro Andrade, Jairo Bonilla, Axel Herrera y Micaela Salcedo.

## Objetivos

**General:**  
Desarrollar un sistema de chat seguro y en tiempo real, con salas administradas mediante PIN, utilizando tecnologías distribuidas.

<details>
<summary><b>Objetivos Específicos:</b></summary>


* Implementar autenticación del administrador.
* Permitir la creación de salas con ID único y PIN.
* Integrar mensajería bidireccional con WebSockets.
* Habilitar salas multimedia para subir archivos.
* Asegurar concurrencia mediante hilos.
* Garantizar sesión única por dispositivo.
* Ofrecer un frontend responsivo y funcional.

</details>

---

<details>
<summary><b> Arquitectura</b></summary>

![](imagenes/Arquitectura.png)

</details>

---

## Metodología

* **Lenguaje y herramientas:**  
  - Spring Boot y MySQL  
  - Docker  
  - Cuenta en Cloudinary (para subir archivos multimedia)

<details>
<summary><b>Requisitos del Sistema</b></summary>

Asegúrate de tener instalado el siguiente software:

* **JDK 21** o superior  
* **Node.js 18+** o superior  
* **Maven 3.9+** o superior  
* **Git**  
* **MySQL 8** (o utilizar la opción recomendada de Docker)  
* **Cuenta en Cloudinary**

</details>

--- 
## Base de Datos con Docker (RECOMENDADO)

La manera más sencilla de levantar la base de datos MySQL es utilizando Docker.

<details>
<summary><b>Ejecución del Proyecto Backend  </b></summary>

## 1. Clonar el Repositorio

```bash
https://github.com/JairoBonilla2004/Chat-Real-TIme.git

```

## 2. Iniciar el Contenedor
Ejecuta el siguiente comando en tu terminal para crear e iniciar el contenedor de la base de datos:

```bash
docker run -d --name mysql-chat -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root123 -e MYSQL_DATABASE=chat_real -e MYSQL_USER=jairo -e MYSQL_PASSWORD=jairo123 --restart=always mysql:8.0 --default-authentication-plugin=mysql_native_password
```
Si el contenedor ya existe, puedes iniciarlo simplemente con: docker start mysql-chat

## 3. Configuración de Conexión
Verifica que el archivo **backend/src/main/resources/application.properties** contenga la configuración de la base de datos:

```
spring.datasource.url=jdbc:mysql://localhost:3306/chat_real
spring.datasource.username=jairo
spring.datasource.password=jairo123
spring.jpa.hibernate.ddl-auto=update

CLOUDINARY_CLOUD_NAME= Colorar tu CLOUD_NAME
CLOUDINARY_API_KEY= coloca tu apy key
CLOUDINARY_API_SECRET=coloca tu apu secret

```

## 4. Ejecutar el Servidor
Desde la carpeta raíz del proyecto **(/chat-real-time)**, navega a la carpeta del backend y ejecuta:

```bash
cd backend
mvn spring-boot:run
```
El backend estará disponible en **http://localhost:8080**.

## 5. Ejecutar el Servidor
Desde la carpeta raíz del proyecto **(/chat-real-time)**, navega a la carpeta del backend y ejecuta:

```bash
cd backend
mvn spring-boot:run
```
El backend estará disponible en **http://localhost:8080**.
</details>

--- 
<details>
<summary><b>Ejecución del Proyecto Frontend </b></summary>

## 1. Clonar el Repositorio
Abre una nueva terminal (manteniendo el backend en ejecución) y navega a la carpeta del frontend.
```bash
cd frontend
npm install
```
## 1. Iniciar la Aplicación

```bash
npm run dev
```

El backend estará disponible en **http://localhost:3000**.
</details>

---

## 🚀 Cómo funciona la aplicación

Esta aplicación está diseñada para facilitar la comunicación instantánea en salas virtuales con distintos niveles de acceso y funcionalidades. A continuación, se detalla el flujo de uso.

---

<details>
<summary>🧑‍💻 Roles de Usuario</summary>

El sistema distingue entre dos roles principales para gestionar el acceso y la funcionalidad de las salas:

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **Administrador** | Usuario registrado y autenticado. | Crear, Ver y Gestionar salas. |
| **Invitado** | Usuario no registrado que accede a una sala. | Unirse a salas existentes (solo necesita la URL y/o PIN). |

</details>

<details>
<summary>🔑 Acceso y Autenticación</summary>

- **Registro:** Para obtener el rol de Administrador, el primer paso es registrarse en la plataforma.
- **Inicio de Sesión:** Una vez registrado, inicia sesión utilizando tu nombre de usuario y contraseña para acceder a las funcionalidades de gestión.

</details>

<details>
<summary>➕ Creación de Salas (Solo Administradores)</summary>

Como Administrador, puedes crear una sala configurando las siguientes características:

**Tipo de Sala:**

- **Multimedia:** Permite a los participantes subir archivos multimedia (imágenes, videos, etc.) además de enviar texto.
- **Solo Texto:** Solo permite el envío de mensajes de texto.

**Nombre de la Sala:**  
Un nombre descriptivo para identificar la reunión.

**Capacidad:**  
El número máximo de personas que pueden unirse a la sala.

**Límite de Carga (Solo Multimedia):**  
El máximo de megabytes (MB) que se permite subir en los archivos multimedia.

</details>

<details>
<summary>📌 PIN de Acceso</summary>

Una vez creada la sala, el sistema te proporcionará un **PIN de Sala**. Este PIN es la clave de acceso que debes compartir con los Invitados.

> 📝 Nota: Si olvidas el PIN de una sala, el sistema te ofrece la opción de restablecerlo por uno nuevo.

</details>

<details>
<summary>➡️Ingreso como Invitado</summary>

Para unirte a una sala como Invitado, no necesitas registrarte, pero debes proporcionar la información de la sala:

- **Nickname:** Un apodo obligatorio que será visible para todos los participantes de la sala.  
- **Nombre de la Sala:** El nombre exacto de la sala.  
- **PIN de Acceso:** El código secreto que debe ser proporcionado por el Administrador de la sala.

</details>

<details>
<summary>⚠️Cosas a Tener en Cuenta</summary>

- **Sesión Volátil:** La sala de chat funciona de manera similar a una reunión de Google Meet:  
  Si recargas la página o cierras la pestaña, saldrás de la sala y tendrás que volver a ingresar con el PIN y el Nickname.
- **Límite de Dispositivos:** Solo se permite ingresar a una reunión por dispositivo a la vez. No puedes unirte a dos salas o a la misma sala dos veces desde el mismo navegador o dispositivo.

</details>

<details>
<summary>➡️Flujo del Programa</summary>

![](imagenes//FlujoSala.png)

</details>