package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forum.entity.ForumReport;
import tn.esprit.forum.entity.ForumReportStatus;

import java.util.List;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {

    List<ForumReport> findByStatus(ForumReportStatus status);

    List<ForumReport> findByPostId(Long postId);
}
