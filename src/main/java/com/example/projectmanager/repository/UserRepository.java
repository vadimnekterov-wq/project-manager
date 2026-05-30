package com.example.projectmanager.repository;

import com.example.projectmanager.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (username, email, password_hash, role, enabled, blocked, blocked_reason, main_admin, confirmation_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getPassword(), user.getRole(),
                    user.isEnabled(), user.isBlocked(), user.getBlockedReason(), user.isMainAdmin(), user.getConfirmationToken());
        } else {
            String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?, enabled = ?, blocked = ?, blocked_reason = ?, confirmation_token = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getPassword(), user.getRole(),
                    user.isEnabled(), user.isBlocked(), user.getBlockedReason(), user.getConfirmationToken(), user.getId());
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);
        return users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), email);
        return users.stream().findFirst();
    }

    public Optional<User> findByConfirmationToken(String token) {
        String sql = "SELECT * FROM users WHERE confirmation_token = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), token);
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);
        return users.stream().findFirst();
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public List<User> findByUsernameContaining(String query) {
        String sql = "SELECT * FROM users WHERE username ILIKE '%' || ? || '%'";
        return jdbcTemplate.query(sql, new UserRowMapper(), query);
    }

    public Set<String> findRoleNamesByUserId(Long userId) {
        String sql = "SELECT r.name FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?";
        List<String> roles = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), userId);
        return roles.stream().collect(Collectors.toSet());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password_hash"));
            user.setRole(rs.getString("role"));
            user.setEnabled(rs.getBoolean("enabled"));
            user.setBlocked(rs.getBoolean("blocked"));
            user.setBlockedReason(rs.getString("blocked_reason"));
            user.setMainAdmin(rs.getBoolean("main_admin"));
            user.setConfirmationToken(rs.getString("confirmation_token"));
            user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            return user;
        }
    }
}
