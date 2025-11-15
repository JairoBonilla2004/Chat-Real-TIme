# Sistema de Chat en Tiempo Real con Salas Seguras


Este proyecto implementa un sistema de chat en tiempo real con salas seguras, desarrollado como parte del Proyecto Integrador del Parcial I de la materia**Aplicaciones Distribuidas**. El sistema permite comunicación instantánea mediante **WebSockets** , salas protegidas con **PIN**. gestión de archivos en salas multimedia y un flujo de interacción administrado por un backend robusto en **Spring Boot**

## Objetivos

**General:** Desarrollar un sistema de chat seguro y en tiempo real, con salas administradas mediante PIN, utilizando tecnologías distribuidas.

**Específicos:**

* Implementar autenticación del administrador.

* Permitir la creación de salas con ID único y PIN.

* Integrar mensajería bidireccional con WebSockets.

* Habilitar salas multimedia para subir archivos.

* Asegurar concurrencia mediante hilos.

* Garantizar sesión única por dispositivo.

* Ofrecer un frontend responsivo y funcional.

## Metodología

* **Lenguaje y herramientas:** 
* Sprintboot y MySql 
* docker 
* cuenta en cloudinary (subir archivos multimedia)

## Requisitos del Sistema

Asegúrate de tener instalado el siguiente software:

* **JDK 21** o superior
* **Node.js 18+** o superior
* **Maven 3.9+** o superior
* **Git**
* **MySQL 8** (o utilizar la opción recomendada de Docker)
* **cuenta en cloudinary**


## Base de Datos con Docker (RECOMENDADO)

La manera más sencilla de levantar la base de datos MySQL es utilizando Docker.


### Ejecución del Proyecto Backend 
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

Bash
```bash
cd backend
mvn spring-boot:run
```
El backend estará disponible en **http://localhost:8080**.

## 5. Ejecutar el Servidor
Desde la carpeta raíz del proyecto **(/chat-real-time)**, navega a la carpeta del backend y ejecuta:

Bash
```bash
cd backend
mvn spring-boot:run
```
El backend estará disponible en **http://localhost:8080**.

### Ejecución del Proyecto Frontend 
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
