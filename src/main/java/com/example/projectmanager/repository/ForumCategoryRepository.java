package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ForumCategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ForumCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ForumCategory category) {
        if (category.getId() == null) {
            String sql = "INSERT INTO forum_categories (project_id, name) VALUES (?, ?)";
            jdbcTemplate.update(sql, category.getProjectId(), category.getName());
        } else {
            String sql = "UPDATE forum_categories SET project_id = ?, name = ? WHERE id = ?";
            jdbcTemplate.update(sql, category.getProjectId(), category.getName(), category.getId());
        }
    }

    public List<ForumCategory> findByProjectId(Long projectId) {
        String sql = "SELECT * FROM forum_categories WHERE project_id = ?";
        return jdbcTemplate.query(sql, new ForumCategoryRowMapper(), projectId);
    }

    public List<ForumCategory> findAll() {
        String sql = "SELECT * FROM forum_categories";
        return jdbcTemplate.query(sql, new ForumCategoryRowMapper());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM forum_categories WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private static class ForumCategoryRowMapper implements RowMapper<ForumCategory> {
        @Override
        public ForumCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
            ForumCategory category = new ForumCategory();
            category.setId(rs.getLong("id"));
            category.setProjectId(rs.getLong("project_id"));
            category.setName(rs.getString("name"));
            return category;
        }
    }
}
