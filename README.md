# 💃 Baila Conmigo

**Baila Conmigo** es una plataforma web para la gestión de clases y eventos de baile. Permite a usuarios registrarse, suscribirse a paquetes, recibir notificaciones por correo electrónico y realizar pagos a través de Mercado Pago.

Este proyecto forma parte de la tesis de grado de Santiago Barrionuevo.

---

## 🧩 Tecnologías Utilizadas

- **Backend:** Java 17 · Spring Boot · Spring Security · JPA · Hibernate
- **Base de Datos:** H2 (en memoria para testing)
- **Frontend:** Angular (versión 18+)
- **Autenticación:** JWT personalizada
- **Correo:** JavaMailSender + Gmail SMTP
- **Pagos:** Mercado Pago SDK
- **Contenedor:** Docker + Docker Compose

---

## 🚀 Funcionalidades Principales

### 👥 Usuarios
- Registro y login con JWT
- Roles personalizados (usuario/admin)
- Gestión de acceso

### 🎫 Eventos y Suscripciones
- Visualización de eventos disponibles
- Suscripción a paquetes de clases
- Cancelación con opción a reembolso

### 💌 Notificaciones por Correo
- Bienvenida al registrarse
- Confirmación de suscripción
- Cancelación con reembolso
- Recuperación de contraseña

### 💳 Integración con Mercado Pago
- Suscripciones y pagos en línea
- Webhooks para confirmación automática
