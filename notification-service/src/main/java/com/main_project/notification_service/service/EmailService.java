package com.main_project.notification_service.service;

import com.main_project.notification_service.exceptions.AppException;
import com.main_project.notification_service.exceptions.enums.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender emailSender;
    SpringTemplateEngine templateEngine;

    public boolean sendHtmlEmail(
            String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            return true;

        } catch (MessagingException e) {
            return false;
//            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }
}