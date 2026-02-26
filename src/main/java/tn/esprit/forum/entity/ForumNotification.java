package tn.esprit.forum.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_notification",
        indexes = {
                @Index(name = "idx_notif_user", columnList = "userId"),
                @Index(name = "idx_notif_read", columnList = "isRead")
        })
public class ForumNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long postId;
    private Long fromUserId;

    private String fromUsername;
    private String fromAvatar;

    @Column(length = 500)
    private String message;

    private String type; // TAG, REPLY, REPORT_UPDATE

    private Boolean isRead = false;

    private LocalDateTime createdAt;

    // ── Constructors ──

    public ForumNotification() {
    }

    public ForumNotification(Long userId, Long postId, Long fromUserId, String fromUsername,
                              String fromAvatar, String message, String type) {
        this.userId = userId;
        this.postId = postId;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.fromAvatar = fromAvatar;
        this.message = message;
        this.type = type;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public String getFromAvatar() { return fromAvatar; }
    public void setFromAvatar(String fromAvatar) { this.fromAvatar = fromAvatar; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
