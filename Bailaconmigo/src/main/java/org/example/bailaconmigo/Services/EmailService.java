package org.example.bailaconmigo.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.EventRegistration;
import org.example.bailaconmigo.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    public void sendWelcomeEmail(String to, String fullName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("¡Bienvenido/a a Baila Conmigo!");

            String htmlContent = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                      <h2 style="color: #d63384;">¡Bienvenido/a a Baila Conmigo!</h2>
                      <p>Hola <strong>%s</strong>,</p>
                      
                      <p>
                        Gracias por registrarte en <strong>Baila Conmigo</strong>, la plataforma donde la danza une personas. Ya formas parte de una comunidad que comparte la pasión por el ritmo y el movimiento.
                      </p>
                      
                      <h3>¿Qué podés hacer ahora?</h3>
                      <ul>
                        <li>🕺 Completar tu perfil de bailarín/a</li>
                        <li>💃 Mostrar tus estilos favoritos y tu nivel</li>
                        <li>📷 Subir fotos y videos de tus mejores pasos</li>
                        <li>📍 Conectarte con otros apasionados de la danza en tu ciudad</li>
                      </ul>
                      
                      <p>
                        ¡No olvides completar tu perfil para que todos puedan conocer tu arte y energía!
                      </p>
                      
                      <p style="margin-top: 30px;">Si tenés alguna pregunta o sugerencia, no dudes en contactarnos.</p>
                      
                      <p style="color: #888;">Con ritmo,</p>
                      <p><strong>El equipo de Baila Conmigo</strong></p>
                      
                      <hr />
                      <small style="color: #aaa;">
                        Este es un correo automático. Por favor, no respondas a este mensaje.
                      </small>
                    </body>
                    </html>
                    """.formatted(fullName);

            helper.setText(htmlContent, true); // true para que se interprete como HTML

            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al enviar el email: " + e.getMessage());
        }
    }

    public void sendNewMessageNotification(String to, String recipientName, String senderName, String messageContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("📩 Nuevo mensaje en Baila Conmigo");

            String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #d63384;">📬 ¡Tenés un nuevo mensaje!</h2>
              <p>Hola <strong>%s</strong>,</p>

              <p>Recibiste un nuevo mensaje de <strong>%s</strong> en la plataforma <strong>Baila Conmigo</strong>:</p>

              <blockquote style="background-color: #f9f9f9; border-left: 4px solid #d63384; margin: 20px; padding: 15px; font-style: italic;">
                %s
              </blockquote>

              <p>¡Iniciá sesión para responder y seguir la conversación!</p>

              <p style="text-align: center; margin: 30px 0;">
                <a href="%s" style="background-color: #d63384; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px;">
                  Ir al chat
                </a>
              </p>

              <p style="color: #888;">Con ritmo,</p>
              <p><strong>El equipo de Baila Conmigo</strong></p>

              <hr />
              <small style="color: #aaa;">
                Este es un correo automático. Por favor, no respondas a este mensaje.
              </small>
            </body>
            </html>
        """.formatted(recipientName, senderName, messageContent, frontendUrl + "/chat");

            helper.setText(htmlContent, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            System.out.println("Error al enviar notificación de nuevo mensaje: " + e.getMessage());
        }
    }


    public void sendPasswordResetEmail(String to, String fullName, String resetToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Recuperación de contraseña - Baila Conmigo");

            // URL del frontend donde el usuario podrá restablecer su contraseña
            // MODIFICAR ESTA URL para que coincida con tu entorno de desarrollo o producción
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                  <h2 style="color: #d63384;">Recuperación de Contraseña</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  
                  <p>
                    Recibimos una solicitud para restablecer la contraseña de tu cuenta en <strong>Baila Conmigo</strong>.
                  </p>
                  
                  <p>
                    Para continuar con el proceso, haz clic en el siguiente botón:
                  </p>
                  
                  <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" 
                       style="background-color: #d63384; 
                              color: white; 
                              padding: 12px 24px; 
                              text-decoration: none; 
                              border-radius: 4px; 
                              font-weight: bold;">
                      Restablecer mi contraseña
                    </a>
                  </p>
                  
                  <p>
                    Este enlace será válido durante 1 hora. Si no solicitaste este cambio, puedes ignorar este correo y tu contraseña permanecerá sin cambios.
                  </p>
                  
                  <p style="margin-top: 30px;">Si tienes problemas con el botón, copia y pega este enlace en tu navegador:</p>
                  <p style="word-break: break-all; color: #666;">%s</p>
                  
                  <p style="color: #888;">Con ritmo,</p>
                  <p><strong>El equipo de Baila Conmigo</strong></p>
                  
                  <hr />
                  <small style="color: #aaa;">
                    Este es un correo automático. Por favor, no respondas a este mensaje.
                  </small>
                </body>
                </html>
                """.formatted(fullName, resetUrl, resetUrl);

            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al enviar el email de recuperación: " + e.getMessage());
        }
    }

    /**
     * Envía recordatorios a todos los inscritos cuando faltan 24 horas para el evento
     */
    /**
     * Envía recordatorios a todos los inscritos cuando faltan 24 horas para el evento
     */
    public void sendEventReminder24Hours(Event event) {
        if (event.getRegistrations() == null || event.getRegistrations().isEmpty()) {
            System.out.println("No hay inscripciones para el evento: " + event.getName());
            return;
        }

        String organizerEmail = event.getOrganizer().getContactEmail();
        String address = event.getAddress() != null ? event.getAddress() : "Ubicación por confirmar";

        for (EventRegistration registration : event.getRegistrations()) {
            User dancer = registration.getDancer();

            // Generar código dinámico para el recordatorio
            String codigoDinamico = generarCodigoDinamicoParaEmail(registration);

            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(dancer.getEmail());
                helper.setSubject("¡Recordatorio! Tu evento es mañana - " + event.getName());

                String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #17a2b8;">🕺 ¡Tu evento es mañana! 💃</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>Te recordamos que <strong>mañana</strong> es el evento <strong>"%s"</strong> al que estás inscrito/a.</p>
              
              <div style="background-color: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <h3 style="color: #1976d2; margin-bottom: 15px;">📋 Detalles del evento:</h3>
                <ul style="list-style: none; padding: 0;">
                  <li style="margin-bottom: 8px;"><strong>📅 Fecha y hora:</strong> %s</li>
                  <li style="margin-bottom: 8px;"><strong>📍 Dirección:</strong> %s</li>
                  <li style="margin-bottom: 8px;"><strong>💰 Precio:</strong> %s</li>
                </ul>
              </div>
              
              <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center; border-left: 4px solid #ffc107;">
                <h3 style="color: #856404; margin-bottom: 10px;">🎫 Tu código de acceso:</h3>
                <div style="font-size: 20px; font-weight: bold; color: #495057; letter-spacing: 1px; 
                            background-color: white; padding: 10px; border-radius: 5px; 
                            border: 2px solid #ffc107;">
                  %s
                </div>
                <p style="margin-top: 10px; font-size: 14px; color: #856404;">
                  <strong>¡No olvides llevarlo!</strong> Lo necesitarás para ingresar al evento.
                </p>
              </div>
              
              <h3 style="color: #28a745;">✅ Preparativos para mañana:</h3>
              <ul>
                <li>🎽 Vestite cómodo y listo para bailar</li>
                <li>💧 Llevá agua para mantenerte hidratado</li>
                <li>📱 Asegurate de tener tu código de acceso a mano</li>
                <li>🚗 Planificá tu llegada con tiempo suficiente</li>
                <li>😊 ¡Vení con la mejor energía!</li>
              </ul>
              
              <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p><strong>💬 ¿Tenés alguna consulta de último momento?</strong></p>
                <p>Contactá al organizador: <strong>%s</strong></p>
              </div>
              
              <p style="text-align: center; margin: 30px 0; font-size: 18px; color: #d63384;">
                <strong>¡Nos vemos mañana en la pista! 🎉</strong>
              </p>
              
              <p style="color: #888;">Con ritmo,</p>
              <p><strong>El equipo de Baila Conmigo</strong></p>
              
              <hr />
              <small style="color: #aaa;">
                Este es un correo automático. Por favor, no respondas a este mensaje.
              </small>
            </body>
            </html>
            """.formatted(
                        dancer.getFullName(),
                        event.getName(),
                        event.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")),
                        address,
                        event.getPrice() != null ? "$" + event.getPrice() : "Gratuito",
                        codigoDinamico,
                        organizerEmail
                );

                helper.setText(htmlContent, true);
                javaMailSender.send(message);

            } catch (MessagingException e) {
                System.out.println("Error al enviar recordatorio 24h a " + dancer.getEmail() + ": " + e.getMessage());
            }
        }

        System.out.println("Recordatorios 24h enviados para el evento: " + event.getName() +
                " (" + event.getRegistrations().size() + " inscriptos)");
    }



    public void sendMatchNotificationEmail(String to, String fullName, String matchedWithName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("¡Nuevo match en Baila Conmigo!");

            String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #28a745;">💃 ¡Es un match! 🕺</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>¡Tenemos buenas noticias! <strong>%s</strong> también te dio like en <strong>Baila Conmigo</strong>. Ya pueden empezar a conectar y compartir su pasión por la danza.</p>

              <p>Iniciá sesión en la app para ver el perfil y empezar a chatear 🎉</p>

              <p style="color: #888;">Con ritmo,</p>
              <p><strong>El equipo de Baila Conmigo</strong></p>
              
              <hr />
              <small style="color: #aaa;">
                Este es un correo automático. Por favor, no respondas a este mensaje.
              </small>
            </body>
            </html>
        """.formatted(fullName, matchedWithName);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al enviar el email de match: " + e.getMessage());
        }
    }

    public void sendSubscriptionConfirmationEmail(String to, String fullName, LocalDate expirationDate) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("¡Tu membresía PRO está activa! - Baila Conmigo");

            String formattedExpirationDate = expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #d63384;">¡Membresía PRO Activada!</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>
                ¡Tu pago ha sido procesado correctamente y tu membresía PRO ya está activa en <strong>Baila Conmigo</strong>!
              </p>
              
              <h3>Detalles de tu suscripción:</h3>
              <ul>
                <li>✅ Tipo de membresía: <strong>PRO</strong></li>
                <li>📅 Fecha de vencimiento: <strong>%s</strong></li>
                <li>⭐ Estado: <strong>Activa</strong></li>
              </ul>
              
              <h3>Ahora disfrutas de estos beneficios:</h3>
              <ul>
                <li>🔍 Mayor visibilidad en las búsquedas</li>
                <li>💬 Mensajes ilimitados con otros bailarines</li>
                <li>🌟 Perfil destacado en la comunidad</li>
                <li>📊 Estadísticas avanzadas de tu perfil</li>
                <li>🎯 Acceso a eventos exclusivos</li>
              </ul>
              
              <p>
                ¡Gracias por confiar en nosotros! Esperamos que disfrutes al máximo de tu experiencia premium.
              </p>
              
              <p style="margin-top: 30px;">Si tienes alguna pregunta sobre tu membresía, no dudes en contactarnos.</p>
              
              <p style="color: #888;">Con ritmo,</p>
              <p><strong>El equipo de Baila Conmigo</strong></p>
              
              <hr />
              <small style="color: #aaa;">
                Este es un correo automático. Por favor, no respondas a este mensaje.
              </small>
            </body>
            </html>
            """.formatted(fullName, formattedExpirationDate);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al enviar el email de confirmación de suscripción: " + e.getMessage());
        }
    }

    /**
     * Genera código dinámico para usar en emails
     */
    private String generarCodigoDinamicoParaEmail(EventRegistration registration) {
        LocalDateTime registrationDate = registration.getRegistrationDate();

        // Formatear fecha como YYYYMMDD
        String fechaFormateada = String.format("%04d%02d%02d",
                registrationDate.getYear(),
                registrationDate.getMonthValue(),
                registrationDate.getDayOfMonth());

        // Obtener iniciales del nombre del bailarín
        String fullName = registration.getDancer().getFullName().trim();
        String[] nombres = fullName.split(" ");
        String iniciales;

        if (nombres.length >= 2) {
            iniciales = nombres[0].substring(0, 1) + nombres[1].substring(0, 1);
        } else {
            iniciales = nombres[0].length() >= 2 ?
                    nombres[0].substring(0, 2) :
                    nombres[0] + "X";
        }

        return fechaFormateada + "-" + iniciales.toUpperCase();
    }



    public void sendRegistrationConfirmationToDancer(EventRegistration registration) {
        User dancer = registration.getDancer();
        Event event = registration.getEvent();

        // ===== GENERAR CÓDIGO DINÁMICO PARA EL EMAIL =====
        String codigoDinamico = generarCodigoDinamicoParaEmail(registration);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dancer.getEmail());
            helper.setSubject("Confirmación de inscripción - " + event.getName());

            String htmlContent = """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
          <h2 style="color: #d63384;">¡Inscripción confirmada!</h2>
          <p>Hola <strong>%s</strong>,</p>
          
          <p>Tu inscripción al evento <strong>"%s"</strong> ha sido confirmada.</p>
          
          <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;">
            <h3 style="color: #d63384; margin-bottom: 10px;">Tu código de inscripción:</h3>
            <div style="font-size: 24px; font-weight: bold; color: #495057; letter-spacing: 2px; 
                        border: 2px dashed #d63384; padding: 15px; border-radius: 5px; 
                        background-color: white;">
              %s
            </div>
            <p style="margin-top: 10px; font-size: 14px; color: #6c757d;">
              <strong>¡Importante!</strong> Presenta este código el día del evento.
            </p>
          </div>
          
          <h3>Detalles del evento:</h3>
          <ul>
            <li><strong>Fecha y hora:</strong> %s</li>
            <li><strong>Lugar:</strong> %s</li>
            <li><strong>Dirección:</strong> %s</li>
          </ul>
          
          <p><strong>Recordá:</strong> Guarda este código ya que lo necesitarás para el acceso al evento.</p>
          
          <p>¡Esperamos verte allí!</p>
          
          <p style="color: #888;">Con ritmo,</p>
          <p><strong>Equipo de Baila Conmigo</strong></p>
          <hr />
          <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
        </body>
        </html>
        """.formatted(
                    dancer.getFullName(),
                    event.getName(),
                    codigoDinamico,  // ===== CÓDIGO DINÁMICO EN EL EMAIL =====
                    event.getDateTime(),
                    event.getAddress(),
                    event.getAddress()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al enviar confirmación: " + e.getMessage());
        }
    }

    public void sendRegistrationNotificationToOrganizer(EventRegistration registration) {
        Event event = registration.getEvent();
        User dancer = registration.getDancer();
        String organizerEmail = event.getOrganizer().getContactEmail();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(organizerEmail);
            helper.setSubject("Nueva inscripción - " + event.getName());

            String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #17a2b8;">Nueva inscripción recibida</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>Un nuevo bailarín se ha inscrito a tu evento <strong>"%s"</strong>.</p>
              
              <h3>Datos del bailarín:</h3>
              <ul>
                <li><strong>Nombre:</strong> %s</li>
                <li><strong>Email:</strong> %s</li>
              </ul>
              
              <p style="color: #888;">Con ritmo,</p>
              <p><strong>Equipo de Baila Conmigo</strong></p>
              <hr />
              <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
            </body>
            </html>
            """.formatted(
                    event.getOrganizer().getOrganizationName(),
                    event.getName(),
                    dancer.getFullName(),
                    dancer.getEmail()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error al notificar al organizador: " + e.getMessage());
        }
    }

    public void notifyEventUpdate(Event event, String updateDetails) {
        // Obtener el correo del organizador
        String organizerEmail = event.getOrganizer().getContactEmail();

        for (EventRegistration registration : event.getRegistrations()) {
            User dancer = registration.getDancer();

            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(dancer.getEmail());
                helper.setSubject("Actualización del evento - " + event.getName());

                String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #ffc107;">Actualización importante</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>El evento <strong>"%s"</strong> al que estás inscrito ha sido actualizado.</p>
              
              <h3>Detalles de la actualización:</h3>
              <p>%s</p>
              
              <h3>Información actualizada del evento:</h3>
              <ul>
                <li><strong>Fecha y hora:</strong> %s</li>
                <li><strong>Lugar:</strong> %s</li>
                <li><strong>Dirección:</strong> %s</li>
              </ul>
              
              <p>Si tenés alguna pregunta, por favor contactá al organizador en: <strong>%s</strong></p>
              
              <p style="color: #888;">Con ritmo,</p>
              <p><strong>Equipo de Baila Conmigo</strong></p>
              <hr />
              <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
            </body>
            </html>
            """.formatted(
                        dancer.getFullName(),
                        event.getName(),
                        updateDetails,
                        event.getDateTime(),
                        event.getAddress(),
                        event.getAddress(),
                        organizerEmail // Añadir el correo del organizador
                );

                helper.setText(htmlContent, true);
                javaMailSender.send(message);
            } catch (MessagingException e) {
                System.out.println("Error al notificar actualización: " + e.getMessage());
            }
        }
    }


    public void notifyEventCancellation(Event event, String cancellationReason) {
        for (EventRegistration registration : event.getRegistrations()) {
            User dancer = registration.getDancer();

            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(dancer.getEmail());
                helper.setSubject("Cancelación del evento - " + event.getName());

                String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
              <h2 style="color: #dc3545;">Cancelación del evento</h2>
              <p>Hola <strong>%s</strong>,</p>
              
              <p>Lamentamos informarte que el evento <strong>"%s"</strong> ha sido cancelado.</p>
              
              <p><strong>Motivo:</strong> %s</p>
              
              <p>Si tenés alguna pregunta, por favor contactá al organizador:</p>
              <ul>
                <li><strong>Email:</strong> %s</li>
                <li><strong>Teléfono:</strong> %s</li>
              </ul>
              
              <p><strong>Importante:</strong> El reembolso se procesará en un plazo de <strong>una semana</strong>.</p>
              
              <p style="color: #888;">Con ritmo,</p>
              <p><strong>Equipo de Baila Conmigo</strong></p>
              <hr />
              <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
            </body>
            </html>
            """.formatted(
                        dancer.getFullName(),
                        event.getName(),
                        cancellationReason,
                        event.getOrganizer().getContactEmail(),
                        event.getOrganizer().getContactPhone()
                );

                helper.setText(htmlContent, true);
                javaMailSender.send(message);
            } catch (MessagingException e) {
                System.out.println("Error al notificar cancelación: " + e.getMessage());
            }
        }
    }

    /**
     * Envía notificaciones de cancelación de evento a múltiples inscripciones
     */
    public void sendEventCancellationNotifications(List<EventRegistration> registrations, String cancellationReason) {
        if (registrations.isEmpty()) {
            return;
        }

        Event event = registrations.get(0).getEvent(); // Todas las inscripciones pertenecen al mismo evento
        String organizerEmail = event.getOrganizer().getContactEmail(); // Obtener el correo del organizador

        for (EventRegistration registration : registrations) {
            User dancer = registration.getDancer();

            // Verificar si hay pago > 0
            boolean hasPaidRegistration = registration.getPaidAmount() != null &&
                    registration.getPaidAmount().compareTo(BigDecimal.ZERO) > 0;

            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(dancer.getEmail());
                helper.setSubject("Cancelación del evento - " + event.getName());

                String refundInfo = hasPaidRegistration
                        ? """
                        <p><strong>Información de reembolso:</strong> El reembolso de tu inscripción será procesado automáticamente y se reflejará en tu cuenta <strong>dentro de 18 días hábiles</strong>.</p>
                      """
                        : "";

                String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                  <h2 style="color: #dc3545;">Cancelación del evento</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  
                  <p>Lamentamos informarte que el evento <strong>"%s"</strong> ha sido cancelado por el organizador.</p>
                  
                  <p><strong>Motivo de cancelación:</strong> %s</p>
                  
                  %s
                  
                  <p>Sentimos las molestias que esto pueda ocasionar. Si tenés alguna pregunta o necesitás más información, por favor contactá al organizador del evento: <strong>%s</strong>.</p>
                  
                  <p style="color: #888;">Con ritmo,</p>
                  <p><strong>Equipo de Baila Conmigo</strong></p>
                  <hr />
                  <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
                </body>
                </html>
                """.formatted(
                        dancer.getFullName(),
                        event.getName(),
                        cancellationReason != null ? cancellationReason : "No especificado",
                        refundInfo,
                        organizerEmail
                );

                helper.setText(htmlContent, true);
                javaMailSender.send(message);

            } catch (MessagingException e) {
                System.out.println("Error al notificar cancelación masiva a " + dancer.getEmail() + ": " + e.getMessage());
            }
        }
    }


    /**
     * Método actualizado para cancelación individual con información de reembolso
     */
    public void sendCancelationPackage(EventRegistration registration, String cancellationReason) {
        User dancer = registration.getDancer();
        Event event = registration.getEvent();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dancer.getEmail());
            helper.setSubject("Cancelación de inscripción - " + event.getName());

            // Determinar si hubo pago para mostrar información de reembolso
            boolean hasPaidRegistration = registration.getPaidAmount() != null &&
                    registration.getPaidAmount().compareTo(BigDecimal.ZERO) > 0;

            String refundInfo = hasPaidRegistration ?
                    "<p><strong>Información de reembolso:</strong> El reembolso de tu inscripción será procesado automáticamente y se reflejará en tu cuenta <strong>dentro de 18 días hábiles</strong>.</p>" :
                    "";

            String htmlContent = """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
          <h2 style="color: #dc3545;">Inscripción Cancelada</h2>
          <p>Hola <strong>%s</strong>,</p>
          
          <p>Tu inscripción al evento <strong>"%s"</strong> ha sido cancelada.</p>
          
          <p><strong>Motivo:</strong> %s</p>
          
          %s
          
          <p>Si tenés alguna consulta, podés contactar al organizador del evento directamente.</p>
          
          <p style="color: #888;">Gracias por tu comprensión,</p>
          <p><strong>El equipo de Baila Conmigo</strong></p>
          <hr />
          <small style="color: #aaa;">Este es un correo automático. Por favor, no respondas a este mensaje.</small>
        </body>
        </html>
        """.formatted(
                    dancer.getFullName(),
                    event.getName(),
                    cancellationReason != null ? cancellationReason : "Cancelación solicitada",
                    refundInfo
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo de cancelación individual: " + e.getMessage());
        }
    }

}
