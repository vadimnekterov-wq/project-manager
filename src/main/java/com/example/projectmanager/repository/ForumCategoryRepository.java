package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumPost;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ForumPostRepository extends CrudRepository<ForumPost, Long> {

    @Query("SELECT * FROM forum_posts WHERE topic_id = :topicId")
    List<ForumPost> findByTopicId(@Param("topicId") Long topicId);
}
