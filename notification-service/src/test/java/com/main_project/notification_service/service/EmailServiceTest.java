package com.main_project.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;
    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("EMAIL-SRV-UT-001 - Send HTML email successfully")
    void sendHtmlEmailShouldReturnTrueWhenMailIsSent() {
        // Note: EMAIL-SRV-UT-001 | Objective: return true when template rendering and email sending both succeed.
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(templateEngine.process(eq("welcome-email"), any(Context.class))).thenReturn("<html>Hello</html>");
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendHtmlEmail("test@example.com", "Hello", "welcome-email", Map.of("name", "A"));

        assertThat(result).isTrue();
        verify(emailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("EMAIL-SRV-UT-002 - Return false when JavaMailSender fails with MessagingException-compatible send path")
    void sendHtmlEmailShouldReturnFalseWhenMessagePreparationFails() {
        // Note: EMAIL-SRV-UT-002 | Objective: return false when email sending flow hits a MessagingException case.
        JavaMailSender failingSender = new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage((Session) null) {
                    @Override
                    public void setSubject(String subject, String charset) throws MessagingException {
                        throw new MessagingException("subject fail");
                    }
                };
            }
            @Override public MimeMessage createMimeMessage(java.io.InputStream contentStream) { return null; }
            @Override public void send(MimeMessage mimeMessage) {}
            @Override public void send(MimeMessage... mimeMessages) {}
            @Override public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {}
            @Override public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) {}
        };

        EmailService service = new EmailService(failingSender, templateEngine);
        when(templateEngine.process(eq("welcome-email"), any(Context.class))).thenReturn("<html>Hello</html>");

        boolean result = service.sendHtmlEmail("test@example.com", "Hello", "welcome-email", Map.of("name", "A"));

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("EMAIL-SRV-UT-003 - Template processing failure should return false")
    void sendHtmlEmailShouldReturnFalseWhenTemplateProcessingFails() {
        // Note: EMAIL-SRV-UT-003 | Objective: gracefully return false when template rendering fails instead of propagating a runtime exception.
        when(templateEngine.process(eq("welcome-email"), any(Context.class))).thenThrow(new RuntimeException("template fail"));

        boolean result = emailService.sendHtmlEmail("test@example.com", "Hello", "welcome-email", Map.of("name", "A"));

        assertThat(result).isFalse();
    }
}
