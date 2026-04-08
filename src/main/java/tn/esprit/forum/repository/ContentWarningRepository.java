package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forum.entity.ContentWarning;
import tn.esprit.forum.entity.ForumReportStatus;

import java.util.List;

@Repository
public interface ContentWarningRepository extends JpaRepository<ContentWarning, Long> {

    List<ContentWarning> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    List<ContentWarning> findByStatus(ForumReportStatus status);

    List<ContentWarning> findAllByOrderByCreatedAtDesc();
}
