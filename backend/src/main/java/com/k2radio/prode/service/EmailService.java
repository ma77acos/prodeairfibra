package com.k2radio.prode.service;

import com.k2radio.prode.entity.PrivateLeague;
import com.k2radio.prode.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ─────────────────────────────────────────────
    // VERIFICACIÓN DE CUENTA
    // ─────────────────────────────────────────────
    @Async
    public void sendVerificationEmail(User user) {
        try {
            String subject = "Verificá tu cuenta - Prode Airfibra Mundial 2026";
            String link = frontendUrl + "/verify-email?token=" + user.getVerificationToken();

            String content = buildEmailTemplate(
                    "Verificá tu cuenta",
                    "¡Hola " + user.getName() + "!",
                    "Gracias por registrarte en el <strong>Prode Airfibra Mundial 2026</strong>.<br>" +
                            "Para activar tu cuenta, hacé clic en el siguiente botón:",
                    "Verificar cuenta",
                    link,
                    "Si vos no creaste esta cuenta, simplemente ignorá este email."
            );

            sendHtmlEmail(user.getEmail(), subject, content);
            log.info("Email de verificación enviado a: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error enviando email de verificación", e);
        }
    }

    // ─────────────────────────────────────────────
    // RECUPERAR CONTRASEÑA
    // ─────────────────────────────────────────────
    @Async
    public void sendPasswordResetEmail(User user) {
        try {
            String subject = "Restablecer contraseña - Prode Airfibra Mundial 2026";
            String link = frontendUrl + "/reset-password?token=" + user.getResetPasswordToken();

            String content = buildEmailTemplate(
                    "Recuperar contraseña",
                    "Hola " + user.getName() + ",",
                    "Recibimos una solicitud para restablecer tu contraseña.<br>" +
                            "Hacé clic en el botón para crear una nueva. <strong>El link expira en 1 hora.</strong>",
                    "Restablecer contraseña",
                    link,
                    "Si vos no solicitaste este cambio, podés ignorar este email. Tu contraseña actual seguirá siendo válida."
            );

            sendHtmlEmail(user.getEmail(), subject, content);
            log.info("Email de reset de contraseña enviado a: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error enviando email de reset de contraseña", e);
        }
    }

    // ─────────────────────────────────────────────
    // INVITACIÓN A LIGA
    // ─────────────────────────────────────────────
    @Async
    public void sendLeagueInvitation(User inviter, PrivateLeague league, String toEmail, String frontendUrl) {
        try {
            String subject = "Te invitaron a una liga - Prode Airfibra Mundial 2026";
            String link = frontendUrl + "/leagues/join/" + league.getCode();

            String content = buildEmailTemplate(
                    "Invitación a liga",
                    "¡Hola!",
                    "<strong>" + inviter.getName() + "</strong> te invitó a unirte a la liga " +
                            "<strong>\"" + league.getName() + "\"</strong> en el Prode Airfibra Mundial 2026.<br><br>" +
                            "O ingresá el código manualmente: <strong>" + league.getCode() + "</strong>",
                    "Unirme a la liga",
                    link,
                    "Si no querés unirte, simplemente ignorá este email."
            );

            sendHtmlEmail(toEmail, subject, content);
            log.info("Invitación de liga enviada a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando invitación de liga", e);
        }
    }

    // ─────────────────────────────────────────────
    // MÉTODO PRIVADO: Enviar HTML
    // ─────────────────────────────────────────────
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML habilitado
        helper.setFrom(new InternetAddress(fromEmail, "Prode Airfibra"));

        mailSender.send(message);
    }

    // ─────────────────────────────────────────────
    // MÉTODO PRIVADO: Template HTML + Firma
    // ─────────────────────────────────────────────
    private String buildEmailTemplate(
            String title,
            String greeting,
            String bodyMessage,
            String buttonText,
            String buttonLink,
            String footerWarning
    ) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background-color:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">

                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f7fb;padding:30px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0"
                                style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.07);">

                                <!-- HEADER -->
                                <tr>
                                    <td style="background:#111827;padding:28px 40px;text-align:center;">
                                        <span style="color:#ffffff;font-size:22px;font-weight:bold;letter-spacing:1px;">
                                            Prode 🌍 Airfibra 
                                        </span>
                                    </td>
                                </tr>

                                <!-- TÍTULO -->
                                <tr>
                                    <td style="padding:32px 40px 0 40px;">
                                        <h1 style="margin:0;font-size:22px;color:#111827;">%s</h1>
                                    </td>
                                </tr>

                                <!-- SALUDO -->
                                <tr>
                                    <td style="padding:16px 40px 0 40px;font-size:15px;color:#333;">
                                        <p style="margin:0;"><strong>%s</strong></p>
                                    </td>
                                </tr>

                                <!-- CUERPO -->
                                <tr>
                                    <td style="padding:16px 40px 24px 40px;font-size:15px;color:#444;line-height:1.7;">
                                        <p style="margin:0;">%s</p>
                                    </td>
                                </tr>

                                <!-- BOTÓN -->
                                <tr>
                                    <td align="center" style="padding:0 40px 32px 40px;">
                                        <a href="%s"
                                           style="display:inline-block;background:#111827;color:#ffffff;
                                                  text-decoration:none;padding:14px 30px;border-radius:8px;
                                                  font-size:15px;font-weight:bold;">
                                            %s
                                        </a>
                                    </td>
                                </tr>

                                <!-- ADVERTENCIA -->
                                <tr>
                                    <td style="padding:0 40px 28px 40px;font-size:13px;color:#888;">
                                        <p style="margin:0;">%s</p>
                                    </td>
                                </tr>

                                <!-- FIRMA / FOOTER -->
                                <tr>
                                    <td style="padding:20px 40px;border-top:1px solid #eaeaea;">
                                        <table cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td style="padding-right:16px;vertical-align:middle;">                                                    
                                                    <img src="https://res.cloudinary.com/dfuzujcgr/image/upload/f_auto,q_auto/WhatsApp_Image_2026-04-24_at_20.40.01_d4h5ug" width="50" style="border-radius:8px;">
                                                </td>
                                                <td style="vertical-align:middle;">
                                                    <p style="margin:0;font-size:14px;color:#111;font-family:Arial,sans-serif;">
                                                        <strong>Marcos Nardelli</strong><br>
                                                        <span style="color:#555;">Founder & Software Developer</span> |                                                        
                                                        <a href="https://instagram.com/fastbuild.dev"
                                                           style="color:#111827;text-decoration:none;">
                                                            <strong>fastbuild.dev</strong>
                                                        </a>
                                                        <br>
                                                        <span style="color:#555;font-size:13px;">Desarrollo de Software a Medida</span><br><br>
                                                        📸
                                                        <a href="https://instagram.com/fastbuild.dev"
                                                           style="color:#111827;text-decoration:none;">
                                                            @fastbuild.dev
                                                        </a>
                                                        &nbsp;&nbsp;
                                                        📱
                                                        <a href="https://wa.me/5492262354827"
                                                           style="color:#111827;text-decoration:none;">
                                                            +54 9 2262 354827
                                                        </a>
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>

            </body>
            </html>
            """.formatted(title, greeting, bodyMessage, buttonLink, buttonText, footerWarning);
    }
}