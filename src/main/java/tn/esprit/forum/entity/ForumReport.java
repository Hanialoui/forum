package tn.esprit.forum.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_report",
        indexes = {
                @Index(name = "idx_post", columnList = "postId"),
                @Index(name = "idx_status", columnList = "status")
        }
)
public class ForumReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long reporterId;
    private Long reportedUserId;

    private String reporterName;
    private String reporterEmail;
    private String reportedUserName;

    private String reason;

    @Column(length = 2000)
    private String postContent;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String adminNote;

    @Enumerated(EnumType.STRING)
    private ForumReportStatus status = ForumReportStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──

    public ForumReport() {
    }

    public ForumReport(Long id, Long postId, Long reporterId, Long reportedUserId,
                       String reporterName, String reporterEmail, String reportedUserName, String reason,
                       String postContent, String description, String adminNote,
                       ForumReportStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.reportedUserName = reportedUserName;
        this.reason = reason;
        this.postContent = postContent;
        this.description = description;
        this.adminNote = adminNote;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Lifecycle callbacks ──

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public ForumReportStatus getStatus() {
        return status;
    }

    public void setStatus(ForumReportStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
