package tn.esprit.forum.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ForumEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ── Report Submitted Confirmation ──
    public void sendReportSubmittedEmail(String toEmail, String reporterName, String reportedUserName,
                                          String reason, String postContent) {
        String truncatedContent = postContent != null && postContent.length() > 120
                ? postContent.substring(0, 120) + "..."
                : (postContent != null ? postContent : "N/A");

        String safeReporterName = escapeHtml(reporterName != null ? reporterName : "User");
        String safeReportedUserName = escapeHtml(reportedUserName != null ? reportedUserName : "Unknown");
        String safeReason = escapeHtml(reason != null ? reason : "Not specified");
        String safeTruncatedContent = escapeHtml(truncatedContent);

        String html = buildEmailWrapper(
            "Report Received",
            "Thank you for keeping our community safe",
            "<p style=\"font-size:16px;color:#1a1a2e;margin:0 0 20px 0;\">Hello <strong>" + safeReporterName + "</strong>,</p>" +
            "<p style=\"font-size:14px;color:#444;line-height:1.7;margin:0 0 24px 0;\">Thank you for taking the time to report content that you believe violates our community guidelines. Your report has been received and our moderation team will review it as soon as possible.</p>" +

            "<div style=\"background:#f8f9fb;border-radius:12px;padding:20px 24px;margin:0 0 24px 0;border-left:4px solid #38a9f3;\">" +
            "  <p style=\"font-size:13px;font-weight:700;color:#38a9f3;text-transform:uppercase;letter-spacing:0.5px;margin:0 0 14px 0;\">Report Summary</p>" +
            "  <table style=\"width:100%;border-collapse:collapse;\">" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;width:130px;\">Reported User</td><td style=\"padding:6px 0;font-size:13px;color:#1a1a2e;font-weight:600;\">" + safeReportedUserName + "</td></tr>" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;\">Reason</td><td style=\"padding:6px 0;font-size:13px;color:#1a1a2e;font-weight:600;\">" + safeReason + "</td></tr>" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;\">Status</td><td style=\"padding:6px 0;\"><span style=\"display:inline-block;background:#fff3e0;color:#f57c00;font-size:11px;font-weight:700;padding:3px 10px;border-radius:20px;\">PENDING REVIEW</span></td></tr>" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;vertical-align:top;\">Content</td><td style=\"padding:6px 0;font-size:13px;color:#666;font-style:italic;\">\"" + safeTruncatedContent + "\"</td></tr>" +
            "  </table>" +
            "</div>" +

            "<p style=\"font-size:14px;color:#444;line-height:1.7;margin:0 0 24px 0;\">We take every report seriously. You will receive a follow-up email once our team has reviewed the content and made a decision.</p>" +

            buildButton("Return to Forums", frontendUrl + "/forums")
        );

        sendHtmlEmail(toEmail, "Your Report Has Been Submitted \u2014 MiNoLingo Community", html);
    }

    // ── Report Decision Email ──
    public void sendReportDecisionEmail(String toEmail, String reporterName, String reportedUserName,
                                         String reason, String status, String adminNote) {

        String safeReporterName = escapeHtml(reporterName != null ? reporterName : "User");
        String safeReportedUserName = escapeHtml(reportedUserName != null ? reportedUserName : "Unknown");
        String safeReason = escapeHtml(reason != null ? reason : "Not specified");

        String statusColor, statusBg, statusLabel, decisionMessage, headerSubtitle, iconEmoji;
        switch (status) {
            case "RESOLVED":
                statusColor = "#2e7d32"; statusBg = "#e8f5e9"; statusLabel = "RESOLVED";
                iconEmoji = "\u2705";
                headerSubtitle = "Action has been taken";
                decisionMessage = "After careful review, our moderation team has <strong>resolved</strong> this report. Appropriate action has been taken against the reported content to ensure our community remains safe and welcoming.";
                break;
            case "DISMISSED":
                statusColor = "#616161"; statusBg = "#f5f5f5"; statusLabel = "DISMISSED";
                iconEmoji = "\u2796";
                headerSubtitle = "Review complete";
                decisionMessage = "After thoroughly reviewing the reported content, our moderation team determined that it <strong>does not violate</strong> our community guidelines. The report has been closed.";
                break;
            case "INVESTIGATING":
                statusColor = "#e65100"; statusBg = "#fff3e0"; statusLabel = "UNDER INVESTIGATION";
                iconEmoji = "\uD83D\uDD0D";
                headerSubtitle = "We're looking into it";
                decisionMessage = "Our moderation team is <strong>actively investigating</strong> this report. We will notify you once a final decision has been reached.";
                break;
            default:
                statusColor = "#1565c0"; statusBg = "#e3f2fd"; statusLabel = status;
                iconEmoji = "\uD83D\uDCCB";
                headerSubtitle = "Status update";
                decisionMessage = "Your report status has been updated to <strong>" + escapeHtml(status) + "</strong>.";
        }

        String adminNoteHtml = "";
        if (adminNote != null && !adminNote.isBlank()) {
            adminNoteHtml =
                "<div style=\"background:#fafafa;border-radius:10px;padding:16px 20px;margin:0 0 24px 0;border:1px solid #e0e0e0;\">" +
                "  <p style=\"font-size:12px;font-weight:700;color:#888;text-transform:uppercase;letter-spacing:0.5px;margin:0 0 8px 0;\">Note from Moderator</p>" +
                "  <p style=\"font-size:14px;color:#333;margin:0;line-height:1.6;font-style:italic;\">\"" + escapeHtml(adminNote) + "\"</p>" +
                "</div>";
        }

        String html = buildEmailWrapper(
            "Report Update " + iconEmoji,
            headerSubtitle,
            "<p style=\"font-size:16px;color:#1a1a2e;margin:0 0 20px 0;\">Hello <strong>" + safeReporterName + "</strong>,</p>" +
            "<p style=\"font-size:14px;color:#444;line-height:1.7;margin:0 0 24px 0;\">We have an update regarding the report you submitted about a post by <strong>" + safeReportedUserName + "</strong>.</p>" +

            "<div style=\"background:#f8f9fb;border-radius:12px;padding:20px 24px;margin:0 0 24px 0;border-left:4px solid " + statusColor + ";\">" +
            "  <p style=\"font-size:13px;font-weight:700;color:" + statusColor + ";text-transform:uppercase;letter-spacing:0.5px;margin:0 0 14px 0;\">Decision</p>" +
            "  <table style=\"width:100%;border-collapse:collapse;\">" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;width:130px;\">Reported User</td><td style=\"padding:6px 0;font-size:13px;color:#1a1a2e;font-weight:600;\">" + safeReportedUserName + "</td></tr>" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;\">Reason</td><td style=\"padding:6px 0;font-size:13px;color:#1a1a2e;font-weight:600;\">" + safeReason + "</td></tr>" +
            "    <tr><td style=\"padding:6px 0;font-size:13px;color:#888;\">Status</td><td style=\"padding:6px 0;\"><span style=\"display:inline-block;background:" + statusBg + ";color:" + statusColor + ";font-size:11px;font-weight:700;padding:3px 10px;border-radius:20px;\">" + statusLabel + "</span></td></tr>" +
            "  </table>" +
            "</div>" +

            adminNoteHtml +

            "<p style=\"font-size:14px;color:#444;line-height:1.7;margin:0 0 24px 0;\">" + decisionMessage + "</p>" +

            buildButton("Return to Forums", frontendUrl + "/forums")
        );

        sendHtmlEmail(toEmail, "Report Update: " + statusLabel + " \u2014 MiNoLingo Community", html);
    }

    // ── Tag Notification Email ──
    public void sendTagNotificationEmail(String toEmail, String taggedUsername, String authorName,
                                          String postContent) {
        String truncatedContent = postContent != null && postContent.length() > 150
                ? postContent.substring(0, 150) + "..."
                : (postContent != null ? postContent : "");

        String safeTaggedUsername = escapeHtml(taggedUsername != null ? taggedUsername : "User");
        String safeAuthorName = escapeHtml(authorName != null ? authorName : "Someone");
        String safeContent = escapeHtml(truncatedContent);

        String html = buildEmailWrapper(
            "You were mentioned!",
            safeAuthorName + " tagged you in a post",
            "<p style=\"font-size:16px;color:#1a1a2e;margin:0 0 20px 0;\">Hello <strong>" + safeTaggedUsername + "</strong>,</p>" +
            "<p style=\"font-size:14px;color:#444;line-height:1.7;margin:0 0 24px 0;\"><strong>" + safeAuthorName + "</strong> mentioned you in a forum post:</p>" +

            "<div style=\"background:#f8f9fb;border-radius:12px;padding:20px 24px;margin:0 0 24px 0;border-left:4px solid #6366f1;\">" +
            "  <p style=\"font-size:14px;color:#333;margin:0;line-height:1.6;font-style:italic;\">\"" + safeContent + "\"</p>" +
            "</div>" +

            buildButton("View Post", frontendUrl + "/forums")
        );

        sendHtmlEmail(toEmail, safeAuthorName + " mentioned you in a post \u2014 MiNoLingo", html);
    }

    // ══════════════════════════════════════════════
    // HTML Email Infrastructure
    // ══════════════════════════════════════════════

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Fallback to simple text email
            SimpleMailMessage fallback = new SimpleMailMessage();
            fallback.setFrom(fromEmail);
            fallback.setTo(toEmail);
            fallback.setSubject(subject);
            fallback.setText("Please view this email in an HTML-capable client.\n\nVisit " + frontendUrl + "/forums for details.");
            mailSender.send(fallback);
        }
    }

    private String buildButton(String label, String url) {
        return "<div style=\"text-align:center;margin:28px 0 8px 0;\">" +
               "  <a href=\"" + url + "\" style=\"display:inline-block;background:linear-gradient(135deg,#38a9f3,#6366f1);color:#ffffff;text-decoration:none;font-size:14px;font-weight:700;padding:12px 36px;border-radius:8px;letter-spacing:0.3px;\"> " + label + "</a>" +
               "</div>";
    }

    private String buildEmailWrapper(String title, String subtitle, String bodyContent) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>" +
            "<body style=\"margin:0;padding:0;background-color:#f0f2f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;\">" +
            "<div style=\"max-width:580px;margin:0 auto;padding:24px 16px;\">" +

            // Header
            "<div style=\"background:linear-gradient(135deg,#38a9f3 0%,#6366f1 100%);border-radius:16px 16px 0 0;padding:32px 32px 28px 32px;text-align:center;\">" +
            "  <div style=\"display:inline-block;width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;line-height:48px;font-size:22px;margin-bottom:16px;\">&#127760;</div>" +
            "  <h1 style=\"color:#ffffff;font-size:22px;font-weight:800;margin:0 0 6px 0;letter-spacing:-0.3px;\">" + title + "</h1>" +
            "  <p style=\"color:rgba(255,255,255,0.8);font-size:13px;margin:0;\">" + subtitle + "</p>" +
            "</div>" +

            // Body
            "<div style=\"background:#ffffff;padding:32px;border-radius:0 0 16px 16px;box-shadow:0 2px 12px rgba(0,0,0,0.06);\">" +
            bodyContent +

            // Divider
            "<div style=\"border-top:1px solid #eee;margin:28px 0 20px 0;\"></div>" +

            // Footer
            "<p style=\"font-size:12px;color:#999;text-align:center;margin:0 0 4px 0;\">This email was sent by MiNoLingo Community Safety Team</p>" +
            "<p style=\"font-size:12px;color:#bbb;text-align:center;margin:0;\">If you did not expect this email, you can safely ignore it.</p>" +
            "</div>" +

            // Branding
            "<div style=\"text-align:center;padding:20px 0;\">" +
            "  <p style=\"font-size:11px;color:#aaa;margin:0;\">MiNoLingo &mdash; Learn Languages Together</p>" +
            "</div>" +

            "</div></body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
