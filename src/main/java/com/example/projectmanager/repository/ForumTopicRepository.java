package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumTopic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ForumTopicRepository {

    private final JdbcTemplate jdbcTemplate;

    public ForumTopicRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ForumTopic topic) {
        if (topic.getId() == null) {
            String sql = "INSERT INTO forum_topics (category_id, title, creator_id) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, topic.getCategoryId(), topic.getTitle(), topic.getCreatorId());
        } else {
            String sql = "UPDATE forum_topics SET category_id = ?, title = ? WHERE id = ?";
            jdbcTemplate.update(sql, topic.getCategoryId(), topic.getTitle(), topic.getId());
        }
    }

    public List<ForumTopic> findByCategoryId(Long categoryId) {
        String sql = "SELECT * FROM forum_topics WHERE category_id = ?";
        return jdbcTemplate.query(sql, new ForumTopicRowMapper(), categoryId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM forum_topics WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public ForumTopic findById(Long id) {
        String sql = "SELECT * FROM forum_topics WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new ForumTopicRowMapper(), id);
    }

    private static class ForumTopicRowMapper implements RowMapper<ForumTopic> {
        @Override
        public ForumTopic mapRow(ResultSet rs, int rowNum) throws SQLException {
            ForumTopic topic = new ForumTopic();
            topic.setId(rs.getLong("id"));
            topic.setCategoryId(rs.getLong("category_id"));
            topic.setTitle(rs.getString("title"));
            topic.setCreatorId(rs.getLong("creator_id"));
            topic.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            return topic;
        }
    }
}
