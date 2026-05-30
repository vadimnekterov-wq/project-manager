package com.example.projectmanager.repository;

import com.example.projectmanager.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Task task) {
        if (task.getId() == null) {
            String sql = "INSERT INTO tasks (project_id, title, description, status, priority, assignee_id, creator_id, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, task.getProjectId(), task.getTitle(), task.getDescription(),
                    task.getStatus(), task.getPriority(), task.getAssigneeId(), task.getCreatorId(), task.getDueDate());
        } else {
            String sql = "UPDATE tasks SET project_id = ?, title = ?, description = ?, status = ?, priority = ?, assignee_id = ?, due_date = ? WHERE id = ?";
            jdbcTemplate.update(sql, task.getProjectId(), task.getTitle(), task.getDescription(),
                    task.getStatus(), task.getPriority(), task.getAssigneeId(), task.getDueDate(), task.getId());
        }
    }

    public List<Task> findByProjectId(Long projectId) {
        String sql = "SELECT * FROM tasks WHERE project_id = ?";
        return jdbcTemplate.query(sql, new TaskRowMapper(), projectId);
    }

    public Task findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new TaskRowMapper(), id);
    }

    public List<Task> search(String query) {
        String sql = "SELECT * FROM tasks WHERE title ILIKE '%' || ? || '%' OR description ILIKE '%' || ? || '%'";
        return jdbcTemplate.query(sql, new TaskRowMapper(), query, query);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private static class TaskRowMapper implements RowMapper<Task> {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setProjectId(rs.getLong("project_id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setPriority(rs.getString("priority"));
            task.setAssigneeId(rs.getObject("assignee_id", Long.class));
            task.setCreatorId(rs.getLong("creator_id"));
            task.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
            task.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            task.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return task;
        }
    }
}
