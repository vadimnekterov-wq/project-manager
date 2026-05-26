package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumPost;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ForumPostRepository extends CrudRepository<ForumPost, Long> {
    List<ForumPost> findByTopicId(Long topicId);
}
