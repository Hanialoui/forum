package tn.esprit.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.ForumReport;
import tn.esprit.forum.entity.ForumReportStatus;
import tn.esprit.forum.repository.ForumReportRepository;
import tn.esprit.forum.repository.ForumRepository;

import java.util.List;

@Service
public class ForumReportService {

    @Autowired
    private ForumReportRepository forumReportRepository;

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private ForumEmailService emailService;

    // ── CREATE ──
    public ForumReport createReport(ForumReport report) {
        System.out.println("[FORUM-REPORT] Creating report - reporterEmail: " + report.getReporterEmail() + ", reporterName: " + report.getReporterName());
        ForumReport saved = forumReportRepository.save(report);
        // Send confirmation email to reporter
        try {
            if (saved.getReporterEmail() != null && !saved.getReporterEmail().isBlank()) {
                System.out.println("[FORUM-REPORT] Sending confirmation email to: " + saved.getReporterEmail());
                emailService.sendReportSubmittedEmail(
                        saved.getReporterEmail(),
                        saved.getReporterName(),
                        saved.getReportedUserName(),
                        saved.getReason(),
                        saved.getPostContent()
                );
            } else {
                System.out.println("[FORUM-REPORT] No reporterEmail provided - skipping email");
            }
        } catch (Exception e) {
            // Log but don't fail the report creation
            System.err.println("Failed to send report confirmation email: " + e.getMessage());
        }
        return saved;
    }

    // ── READ ──
    public List<ForumReport> getAllReports() {
        return forumReportRepository.findAll();
    }

    public ForumReport getReportById(Long id) {
        return forumReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    public List<ForumReport> getReportsByStatus(String status) {
        return forumReportRepository.findByStatus(ForumReportStatus.valueOf(status));
    }

    public List<ForumReport> getReportsByPostId(Long postId) {
        return forumReportRepository.findByPostId(postId);
    }

    // ── UPDATE STATUS ──
    public ForumReport updateReportStatus(Long id, String status, String adminNote) {
        ForumReport existing = getReportById(id);
        existing.setStatus(ForumReportStatus.valueOf(status));
        if (adminNote != null) {
            existing.setAdminNote(adminNote);
        }
        ForumReport saved = forumReportRepository.save(existing);
        // Send decision email to reporter
        try {
            if (saved.getReporterEmail() != null && !saved.getReporterEmail().isBlank()) {
                emailService.sendReportDecisionEmail(
                        saved.getReporterEmail(),
                        saved.getReporterName(),
                        saved.getReportedUserName(),
                        saved.getReason(),
                        status,
                        adminNote
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send report decision email: " + e.getMessage());
        }
        return saved;
    }

    // ── DELETE ──
    public void deleteReport(Long id) {
        if (!forumReportRepository.existsById(id)) {
            throw new RuntimeException("Report not found with id: " + id);
        }
        forumReportRepository.deleteById(id);
    }

    // ── DELETE REPORTED POST ──
    public void deleteReportedPost(Long postId) {
        if (!forumRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }
        forumRepository.deleteById(postId);
    }
}
