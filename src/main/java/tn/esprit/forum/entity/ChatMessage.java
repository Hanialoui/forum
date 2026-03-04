package tn.esprit.forum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private String senderName;
    @Column(columnDefinition = "TEXT")
    private String senderAvatar;

    private Long receiverId;
    private String receiverName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String gifUrl;

    private Long sharedPostId;

    // Reply support
    private Long replyToId;
    @Column(columnDefinition = "TEXT")
    private String replyToContent;
    private String replyToSenderName;

    // Reactions (JSON: {"userId:emoji": true})
    @Column(columnDefinition = "TEXT")
    private String reactions;

    // Soft delete support
    @Column(columnDefinition = "TEXT")
    private String deletedForUsers;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
