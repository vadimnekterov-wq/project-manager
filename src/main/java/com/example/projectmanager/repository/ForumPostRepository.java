package com.example.projectmanager.repository;

import com.example.projectmanager.model.ForumPost;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ForumPostRepository {

    private final JdbcTemplate jdbcTemplate;

    public ForumPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ForumPost post) {
        if (post.getId() == null) {
            String sql = "INSERT INTO forum_posts (topic_id, user_id, body, created_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, post.getTopicId(), post.getUserId(), post.getBody(), post.getCreatedAt());
        } else {
            String sql = "UPDATE forum_posts SET topic_id = ?, user_id = ?, body = ? WHERE id = ?";
            jdbcTemplate.update(sql, post.getTopicId(), post.getUserId(), post.getBody(), post.getId());
        }
    }

    public List<ForumPost> findByTopicId(Long topicId) {
        String sql = "SELECT * FROM forum_posts WHERE topic_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, new ForumPostRowMapper(), topicId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM forum_posts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private static class ForumPostRowMapper implements RowMapper<ForumPost> {
        @Override
        public ForumPost mapRow(ResultSet rs, int rowNum) throws SQLException {
            ForumPost post = new ForumPost();
            post.setId(rs.getLong("id"));
            post.setTopicId(rs.getLong("topic_id"));
            post.setUserId(rs.getLong("user_id"));
            post.setBody(rs.getString("body"));
            post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return post;
        }
    }
}
