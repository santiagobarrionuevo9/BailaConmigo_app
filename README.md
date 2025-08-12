# 💃 Baila Conmigo

**Baila Conmigo** es una plataforma web integral para la gestión de clases y eventos de baile. Además de la inscripción y pagos, permite a los alumnos **buscar pareja de baile mediante un sistema de match** y **chatear en tiempo real** con sus coincidencias. Incluye notificaciones por correo y pagos integrados con Mercado Pago.

Este proyecto forma parte de la tesis de grado de Santiago Barrionuevo.

---

## 🧩 Tecnologías Utilizadas

- **Backend:** Java 17 · Spring Boot · Spring Security · JPA · Hibernate
- **Base de Datos:** H2 (modo test) / PostgreSQL (modo producción)
- **Frontend:** Angular (v18+)
- **Autenticación:** JWT personalizada
- **Correo:** JavaMailSender + Gmail SMTP
- **Pagos:** Mercado Pago SDK
- **Chat:** WebSocket + STOMP
- **Contenedor:** Docker + Docker Compose

---

## 🚀 Funcionalidades Principales

### 👥 Gestión de Usuarios
- Registro/login con JWT
- Roles: Alumno y Administrador
- Perfil editable

### 🎫 Clases y Eventos
- Lista de eventos disponibles
- Inscripción con pago online
- Cancelación con reembolso

### 💌 Notificaciones por Correo
- Bienvenida
- Confirmación de inscripción
- Cancelación
- Recuperación de contraseña

### 💳 Pagos con Mercado Pago
- Checkout personalizado
- Confirmación automática vía webhook
- Soporte para reembolsos

---

### ❤️ Búsqueda de Pareja (Match)

- Cada alumno puede configurar su perfil de match (rol de baile, nivel, disponibilidad).
- Se muestran posibles coincidencias compatibles.
- Si dos personas se gustan, se genera un **match**.
- Al hacer match, se habilita un **chat privado en tiempo real**.

### 💬 Chat en Tiempo Real

- Permite conversar con parejas de baile (match)
- Notificaciones visuales de mensajes nuevos
- Mensajería segura y moderada

