package com.example.projectmanager.repository;

import com.example.projectmanager.model.Task;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssigneeId(Long assigneeId);

    @Query("SELECT * FROM tasks WHERE title LIKE CONCAT('%', :query, '%') OR description LIKE CONCAT('%', :query, '%')")
    List<Task> search(@Param("query") String query);

    @Query("SELECT status, COUNT(*) FROM tasks WHERE project_id = :projectId GROUP BY status")
    List<Object[]> getStatisticsByProject(@Param("projectId") Long projectId);
}