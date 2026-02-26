package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forum.entity.Forum;

import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {

    List<Forum> findByTopicId(Long topicId);

    List<Forum> findByUserId(Long userId);

    List<Forum> findByParentPostId(Long parentPostId);

    List<Forum> findByParentPostIdIsNullOrderByCreatedAtDesc();
}
