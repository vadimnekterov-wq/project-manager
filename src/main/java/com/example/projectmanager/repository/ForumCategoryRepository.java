package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumCategory;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ForumCategoryRepository extends CrudRepository<ForumCategory, Long> {
    List<ForumCategory> findByProjectId(Long projectId);
}
