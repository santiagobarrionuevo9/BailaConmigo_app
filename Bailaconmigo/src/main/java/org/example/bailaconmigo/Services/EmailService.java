package org.example.bailaconmigo.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

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

    public void sendPasswordResetEmail(String to, String fullName, String resetToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Recuperación de contraseña - Baila Conmigo");

            // URL del frontend donde el usuario podrá restablecer su contraseña
            String resetUrl = "https://bailaconmigo-app.com/reset-password?token=" + resetToken;

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
}
