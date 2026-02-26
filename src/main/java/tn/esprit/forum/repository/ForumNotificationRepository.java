package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forum.entity.ForumNotification;

import java.util.List;

@Repository
public interface ForumNotificationRepository extends JpaRepository<ForumNotification, Long> {

    List<ForumNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ForumNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}
