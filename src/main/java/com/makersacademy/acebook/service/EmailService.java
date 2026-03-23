package com.makersacademy.acebook.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final String LOGO_CONTENT_ID = "snailmail-logo";

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String smtpUsername;
    private final String smtpPassword;

    // Constructor injection (recommended)
    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from:}") String fromEmail,
                        @Value("${spring.mail.username:}") String smtpUsername,
                        @Value("${spring.mail.password:}") String smtpPassword) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public boolean hasMailConfiguration() {
        return smtpUsername != null && !smtpUsername.isBlank()
                && smtpPassword != null && !smtpPassword.isBlank()
                && fromEmail != null && !fromEmail.isBlank();
    }

    // Send HTML invite email
    public boolean sendInvite(String toEmail, String groupName, String inviteLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setSubject("You're invited to join " + groupName);

            String htmlMsg = buildEmailHtml(
                    "You’re invited",
                    "You've been invited to join <strong>" + groupName + "</strong>.",
                    "Join circle",
                    inviteLink,
                    "Open the invite link to join and start sharing updates together."
            );

            helper.setText(htmlMsg, true); // true = HTML
            addLogo(helper);
            mailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendPromptFormEmail(String toEmail, String groupName, String frequencyLabel, String promptFormLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setSubject("Your " + frequencyLabel + " prompts for " + groupName);

            String htmlMsg = buildEmailHtml(
                    "Your " + frequencyLabel + " prompts are here",
                    "It’s time to fill in your <strong>" + frequencyLabel + "</strong> update for <strong>" + groupName + "</strong>.",
                    "Open form",
                    promptFormLink,
                    "You can also open the form from inside the app whenever you’re signed in."
            );

            helper.setText(htmlMsg, true);
            addLogo(helper);
            mailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendFeedReadyEmail(String toEmail, String groupName, String frequencyLabel, String feedLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setSubject("Your " + frequencyLabel + " issue for " + groupName + " is ready");

            String htmlMsg = buildEmailHtml(
                    "Your issue is ready",
                    "Your <strong>" + frequencyLabel + "</strong> issue for <strong>" + groupName + "</strong> is now ready to view.",
                    "Open feed",
                    feedLink,
                    "It’s filled with everyone’s responses from this round."
            );

            helper.setText(htmlMsg, true);
            addLogo(helper);
            mailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Send simple plain text email (general purpose)
    public boolean sendSimpleEmail(String[] to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("🐌 " + subject);
            message.setText(body);
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildEmailHtml(String heading,
                                  String introText,
                                  String buttonLabel,
                                  String buttonLink,
                                  String footerText) {
        return """
                <div style="margin:0;padding:32px 16px;background:#fbf9f6;font-family:Georgia,'Times New Roman',serif;color:#3a3a3a;">
                  <div style="max-width:620px;margin:0 auto;background:#ffffff;border:1px solid #e7e0d7;border-radius:28px;box-shadow:0 16px 36px rgba(95,87,78,0.08);overflow:hidden;">
                    <div style="padding:28px 28px 12px;text-align:center;background:linear-gradient(180deg,#fffaf3 0%%,#f7efe6 100%%);">
                      <img src="cid:%s" alt="Snail Mail" style="max-width:260px;width:100%%;height:auto;display:block;margin:0 auto 12px;">
                    </div>
                    <div style="padding:12px 32px 32px;text-align:center;">
                      <h1 style="margin:0 0 16px;font-size:32px;line-height:1.15;color:#3f3d3b;">%s</h1>
                      <p style="margin:0 0 26px;font-size:18px;line-height:1.7;color:#5f5a54;">%s</p>
                      <div style="margin:0 0 28px;">
                        <a href="%s" style="display:inline-block;padding:14px 28px;border-radius:999px;background:linear-gradient(180deg,#cfe1ea 0%%,#bdd2df 100%%);border:2px solid #ffffff;color:#3a3a3a;text-decoration:none;font-size:22px;font-family:'Brush Script MT','Segoe Script',cursive;box-shadow:0 8px 18px rgba(95,87,78,0.12);">
                          %s
                        </a>
                      </div>
                      <p style="margin:0;font-size:15px;line-height:1.7;color:#8b8680;">%s</p>
                    </div>
                  </div>
                </div>
                """.formatted(LOGO_CONTENT_ID, heading, introText, buttonLink, buttonLabel, footerText);
    }

    private void addLogo(MimeMessageHelper helper) throws MessagingException {
        ClassPathResource logo = new ClassPathResource("static/images/snailmail_logo.png");
        if (logo.exists()) {
            helper.addInline(LOGO_CONTENT_ID, logo, "image/png");
        }
    }
}
