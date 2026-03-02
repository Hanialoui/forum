package tn.esprit.forum.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "forum",
        indexes = {
                @Index(name = "idx_topic", columnList = "topicId"),
                @Index(name = "idx_user", columnList = "userId"),
                @Index(name = "idx_parent", columnList = "parentPostId")
        }
)
public class Forum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References (microservice friendly – no ManyToOne)
    private Long topicId;
    private Long userId;

    private String author;
    private String username;
    private String avatar;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String image;

    private Boolean isEdited = false;

    // Self-reference for replies
    private Long parentPostId;

    // Shared post reference (Facebook-style share)
    private Long sharedPostId;

    private Integer comments = 0;

    private Integer reposts = 0;

    private Integer likes = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──

    public Forum() {
    }

    public Forum(Long id, Long topicId, Long userId, String author, String username, String avatar,
                 String content, String image, Boolean isEdited, Long parentPostId, Long sharedPostId,
                 Integer comments, Integer reposts, Integer likes,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.topicId = topicId;
        this.userId = userId;
        this.author = author;
        this.username = username;
        this.avatar = avatar;
        this.content = content;
        this.image = image;
        this.isEdited = isEdited;
        this.parentPostId = parentPostId;
        this.sharedPostId = sharedPostId;
        this.comments = comments;
        this.reposts = reposts;
        this.likes = likes;
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

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public Long getParentPostId() {
        return parentPostId;
    }

    public void setParentPostId(Long parentPostId) {
        this.parentPostId = parentPostId;
    }

    public Long getSharedPostId() {
        return sharedPostId;
    }

    public void setSharedPostId(Long sharedPostId) {
        this.sharedPostId = sharedPostId;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Integer getReposts() {
        return reposts;
    }

    public void setReposts(Integer reposts) {
        this.reposts = reposts;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
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
