package tn.esprit.forum.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_warning",
        indexes = {
                @Index(name = "idx_cw_user", columnList = "userId"),
                @Index(name = "idx_cw_status", columnList = "status")
        }
)
public class ContentWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    @Column(columnDefinition = "TEXT")
    private String blockedContent;

    private String category;       // e.g. PROFANITY, BULLYING, HATE_SPEECH, TOXICITY, INAPPROPRIATE

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    private Integer offenseNumber;  // 1 = first warning, 2 = email sent, 3+ = repeated

    private Boolean emailSent = false;

    @Enumerated(EnumType.STRING)
    private ForumReportStatus status = ForumReportStatus.PENDING;

    @Column(length = 1000)
    private String adminNote;

    private LocalDateTime createdAt;

    // ── Constructors ──

    public ContentWarning() {}

    // ── Lifecycle ──

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getBlockedContent() { return blockedContent; }
    public void setBlockedContent(String blockedContent) { this.blockedContent = blockedContent; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public Integer getOffenseNumber() { return offenseNumber; }
    public void setOffenseNumber(Integer offenseNumber) { this.offenseNumber = offenseNumber; }

    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }

    public ForumReportStatus getStatus() { return status; }
    public void setStatus(ForumReportStatus status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
