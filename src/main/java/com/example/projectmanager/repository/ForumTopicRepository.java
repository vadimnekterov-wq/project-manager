package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumTopic;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ForumTopicRepository extends CrudRepository<ForumTopic, Long> {
    List<ForumTopic> findByCategoryId(Long categoryId);
}
