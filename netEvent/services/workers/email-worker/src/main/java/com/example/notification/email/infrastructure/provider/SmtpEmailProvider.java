package com.example.notification.email.infrastructure.provider;

import com.example.notification.contract.delivery.AttachmentRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.mail.internet.MimeMessage;

@Component
public class SmtpEmailProvider implements EmailProvider {
    private final JavaMailSender mailSender;
    private final Path storageRoot;

    public SmtpEmailProvider(JavaMailSender mailSender,
                             @Value("${app.report.storage.base-path:./data/reports}") String basePath) {
        this.mailSender = mailSender;
        this.storageRoot = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        return sendEmail(to, subject, body, List.of());
    }

    @Override
    public boolean sendEmail(String to, String subject, String body, List<AttachmentRef> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            if (attachments != null) for (AttachmentRef attachment : attachments) {
                Path file = storageRoot.resolve(attachment.getStorageKey()).normalize();
                if (!file.startsWith(storageRoot) || !java.nio.file.Files.isRegularFile(file)) {
                    throw new IllegalStateException("Attachment is unavailable: " + attachment.getStorageKey());
                }
                helper.addAttachment(attachment.getFileName(), new FileSystemResource(file));
            }
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
