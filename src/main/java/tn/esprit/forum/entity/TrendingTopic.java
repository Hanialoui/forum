package tn.esprit.forum.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trending_topics",
       indexes = {
           @Index(name = "idx_category", columnList = "category"),
           @Index(name = "idx_title", columnList = "title")
       })
public class TrendingTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;   // Grammar, TOEFL, Event...
    private String title;      // #PastParticiple, TOEFL Prep...

    private Boolean isPinned = false;

    private Integer viewCount = 0;

    private Integer postCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──

    public TrendingTopic() {
    }

    public TrendingTopic(Long id, String category, String title, Boolean isPinned,
                         Integer viewCount, Integer postCount,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.isPinned = isPinned;
        this.viewCount = viewCount;
        this.postCount = postCount;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
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
