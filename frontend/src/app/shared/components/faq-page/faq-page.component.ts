import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-faq-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './faq-page.component.html',
  styleUrl: './faq-page.component.css'
})
export class FaqPageComponent {
  faqs = [
    {
      pregunta: '¿Qué es BailaConmigo?',
      respuesta: 'Es una plataforma diseñada para conectar bailarines de todos los niveles y estilos, facilitando el encuentro con compañeros, inscripción a clases, participación en eventos y mucho más.',
      abierta: false
    },
    {
      pregunta: '¿Cómo funciona el sistema de emparejamiento?',
      respuesta: 'Completás tu perfil indicando tu estilo de baile, nivel, disponibilidad y ubicación. La plataforma te sugiere compañeros con intereses compatibles. Si ambos dan "like", se genera un match y pueden chatear dentro de la app.',
      abierta: false
    },
    {
      pregunta: '¿Qué puedo hacer con la cuenta gratuita?',
      respuesta: 'Con el acceso básico podés hacer hasta 3 matches por día, dar hasta 20 likes y buscar bailarines con filtros básicos como estilo y ubicación general.',
      abierta: false
    },
    {
      pregunta: '¿Qué beneficios tiene la membresía premium?',
      respuesta: `
        Incluye:
        <ul>
          <li>Likes ilimitados y prioridad en los matches.</li>
          <li>Descuentos en eventos y clases seleccionadas.</li>
          <li>Filtros avanzados (nivel de experiencia, ubicación exacta, disponibilidad horaria).</li>
        </ul>
      `,
      abierta: false
    },
    {
      pregunta: '¿Cómo me inscribo a clases o eventos?',
      respuesta: 'Desde la app podés ver los eventos y clases disponibles, consultar detalles como lugar, horario y costo, e inscribirte directamente pagando con Mercado Pago.',
      abierta: false
    },
    {
      pregunta: '¿Qué métodos de pago acepta BailaConmigo?',
      respuesta: 'Todos los pagos se realizan mediante <strong>Mercado Pago</strong>, permitiendo tarjetas de crédito, débito y transferencias bancarias. La plataforma garantiza la seguridad de las transacciones.',
      abierta: false
    },
    {
      pregunta: '¿Puedo cancelar mi inscripción a un evento o clase?',
      respuesta: 'Sí. Si el organizador cancela el evento, recibirás un reembolso automático en 5 días hábiles. Si el evento cambia de horario o ubicación y no podés asistir, podés solicitar el reembolso en las primeras 24 horas desde el aviso.',
      abierta: false
    },
    {
      pregunta: '¿Qué tipo de perfiles puedo crear?',
      respuesta: 'Cada usuario puede crear un perfil personalizado con información sobre su experiencia, horarios disponibles, ubicación y subir videos de sus presentaciones.',
      abierta: false
    },
    {
      pregunta: '¿Puedo crear eventos o clases si soy organizador?',
      respuesta: 'Sí, si sos organizador, podés registrar eventos detallando lugar, hora, cupos y costo. Los usuarios podrán inscribirse y pagar desde la plataforma.',
      abierta: false
    },
    {
      pregunta: '¿Hay sistema de reseñas?',
      respuesta: 'Sí, los usuarios pueden calificar a otros bailarines, eventos y clases. Esto ayuda a mantener una comunidad segura y confiable.',
      abierta: false
    },
    {
      pregunta: '¿Qué tipo de notificaciones envía la app?',
      respuesta: `
        <ul>
          <li>Nuevo match.</li>
          <li>Mensajes recibidos.</li>
          <li>Recordatorios de eventos y clases.</li>
          <li>Confirmaciones de inscripción.</li>
          <li>Cambios en eventos o clases.</li>
          <li>Reseñas recibidas.</li>
        </ul>
        Se envían por la app, email o WhatsApp (si el usuario lo permite).
      `,
      abierta: false
    },
    {
      pregunta: '¿Qué estadísticas y reportes ofrece la plataforma?',
      respuesta: `
        <ul>
          <li>Estadísticas de matches y mensajes.</li>
          <li>Participación en clases y eventos.</li>
          <li>Conversión de usuarios gratuitos a premium.</li>
          <li>Eventos y profesores mejor calificados.</li>
          <li>Ingresos por membresías y pagos realizados.</li>
        </ul>
      `,
      abierta: false
    },
    {
      pregunta: '¿Qué APIs externas se integran?',
      respuesta: `
        <ul>
          <li><strong>Mercado Pago:</strong> para pagos y reembolsos.</li>
          <li><strong>Google Maps API:</strong> para ubicación precisa.</li>
          <li><strong>Twilio/WhatsApp API:</strong> para notificaciones opcionales por WhatsApp.</li>
          <li><strong>SendGrid/SMTP:</strong> para envío de emails.</li>
        </ul>
      `,
      abierta: false
    },
    {
      pregunta: '¿Qué roles existen en la plataforma?',
      respuesta: `
        <ul>
          <li><strong>Usuario Básico:</strong> acceso gratuito limitado.</li>
          <li><strong>Usuario Premium:</strong> acceso a funcionalidades avanzadas.</li>
          <li><strong>Organizador:</strong> puede crear y gestionar eventos o clases.</li>
          <li><strong>Administrador:</strong> supervisa usuarios y contenido.</li>
        </ul>
      `,
      abierta: false
    },
    {
      pregunta: '¿Cuál es el objetivo principal de BailaConmigo?',
      respuesta: 'Promover la conexión entre bailarines, facilitar el acceso a eventos y clases, y construir una comunidad activa y segura en torno a la pasión por el baile.',
      abierta: false
    }
  ];

  togglePregunta(index: number): void {
    this.faqs[index].abierta = !this.faqs[index].abierta;
  }
}