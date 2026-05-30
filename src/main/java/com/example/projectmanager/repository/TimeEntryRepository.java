package com.example.projectmanager.repository;

import com.example.projectmanager.model.TimeEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TimeEntryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TimeEntryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TimeEntry entry) {
        if (entry.getId() == null) {
            String sql = "INSERT INTO time_entries (task_id, user_id, start_time, end_time) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, entry.getTaskId(), entry.getUserId(), entry.getStartTime(), entry.getEndTime());
        } else {
            String sql = "UPDATE time_entries SET end_time = ? WHERE id = ?";
            jdbcTemplate.update(sql, entry.getEndTime(), entry.getId());
        }
    }

    public List<TimeEntry> findByTaskId(Long taskId) {
        String sql = "SELECT * FROM time_entries WHERE task_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new TimeEntryRowMapper(), taskId);
    }

    public TimeEntry findById(Long id) {
        String sql = "SELECT * FROM time_entries WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new TimeEntryRowMapper(), id);
    }

    public Long getTotalMinutesByTask(Long taskId) {
        String sql = "SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (COALESCE(end_time, NOW()) - start_time)) / 60), 0) FROM time_entries WHERE task_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, taskId);
    }

    private static class TimeEntryRowMapper implements RowMapper<TimeEntry> {
        @Override
        public TimeEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeEntry entry = new TimeEntry();
            entry.setId(rs.getLong("id"));
            entry.setTaskId(rs.getLong("task_id"));
            entry.setUserId(rs.getLong("user_id"));
            entry.setStartTime(rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null);
            entry.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null);
            return entry;
        }
    }
}
