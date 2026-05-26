package com.example.projectmanager.repository;

import com.example.projectmanager.model.Project;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);

    @Query("SELECT p.* FROM projects p JOIN project_members pm ON p.id = pm.project_id WHERE pm.user_id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);
}