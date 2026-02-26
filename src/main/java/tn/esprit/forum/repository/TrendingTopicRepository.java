package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forum.entity.TrendingTopic;

import java.util.List;

@Repository
public interface TrendingTopicRepository extends JpaRepository<TrendingTopic, Long> {

    List<TrendingTopic> findByCategory(String category);

    List<TrendingTopic> findByIsPinnedTrue();

    List<TrendingTopic> findAllByOrderByPostCountDesc();

    List<TrendingTopic> findAllByOrderByPostCountDescViewCountDesc();
}
