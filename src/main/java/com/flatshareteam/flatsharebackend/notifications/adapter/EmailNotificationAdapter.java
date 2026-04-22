package com.flatshareteam.flatsharebackend.notifications.adapter;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Map;
import java.util.List;
import com.flatshareteam.flatsharebackend.notifications.emailtemplate.EmailTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationAdapter implements INotificationPort {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final List<EmailTemplate> templates;

    @Value("${MAIL_USERNAME:}")
    private String smtpUsername;

    private String getSenderEmail() {
        return (smtpUsername != null && !smtpUsername.isBlank()) ? smtpUsername : "noreply@flatshare.com";
    }

    @Async
    @Override
    public void notify(UUID userId, NotificationData message) {
        log.info("--- WYKONYWANIE ASYNCHRONICZNEJ WYSYŁKI E-MAIL ---");

        if (smtpUsername == null || smtpUsername.isBlank() || smtpUsername.equals("Wklej_tu_username_z_mailtrap")) {
            log.info("Pomijam wysyłanie e-mail ({}). Brak skonfigurowanych zmiennych środowiskowych SMTP.", message.getType());
            return;
        }
        

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Nie znaleziono użytkownika o ID: {}. Perywano wysyłkę e-mail.", userId);
            return;
        }

        String recipientEmail = user.getEmail();
        log.info("Odbiorca: {}", recipientEmail);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(getSenderEmail());
            helper.setTo(recipientEmail);
            

            EmailTemplate template = templates.stream()
                    .filter(t -> t.supports(message.getType()))
                    .findFirst()
                    .orElse(null);

            if (template == null) {
                log.warn("Brak wspieranego szablonu e-mail dla typu powiadomienia: {}", message.getType());
                return;
            }

            String subject = template.buildSubject(message.getPayload());
            String htmlBody = template.buildHtmlBody(message.getPayload());

            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(mimeMessage);
            log.info("--- WIADOMOŚĆ WYSŁANA POMYŚLNIE ---");
        } catch (MessagingException e) {
            log.error("Błąd podczas konfiguracji wiadomości e-mail", e);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłki wiadomości e-mail", e);
        }
    }
}
