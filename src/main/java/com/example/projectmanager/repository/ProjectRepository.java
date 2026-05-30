package com.example.projectmanager.repository;

import com.example.projectmanager.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Project project) {
        if (project.getId() == null) {
            String sql = "INSERT INTO projects (name, description, owner_id) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, project.getName(), project.getDescription(), project.getOwnerId());
        } else {
            String sql = "UPDATE projects SET name = ?, description = ?, owner_id = ? WHERE id = ?";
            jdbcTemplate.update(sql, project.getName(), project.getDescription(), project.getOwnerId(), project.getId());
        }
    }

    public List<Project> findAll() {
        String sql = "SELECT * FROM projects";
        return jdbcTemplate.query(sql, new ProjectRowMapper());
    }

    public Project findById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new ProjectRowMapper(), id);
    }

    public List<Project> findByOwnerId(Long ownerId) {
        String sql = "SELECT * FROM projects WHERE owner_id = ?";
        return jdbcTemplate.query(sql, new ProjectRowMapper(), ownerId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private static class ProjectRowMapper implements RowMapper<Project> {
        @Override
        public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
            Project project = new Project();
            project.setId(rs.getLong("id"));
            project.setName(rs.getString("name"));
            project.setDescription(rs.getString("description"));
            project.setOwnerId(rs.getLong("owner_id"));
            project.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            return project;
        }
    }
}
