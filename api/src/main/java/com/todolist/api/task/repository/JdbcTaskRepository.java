package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcTaskRepository implements TaskRepository {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Task> findAll(String tokenKey) {
        String sql = """
            SELECT id, token_key, title, status, priority, due_at, created_at, updated_at, completed_at
            FROM tasks
            WHERE token_key = ?
            ORDER BY updated_at ASC, id ASC
            """;
        return jdbcTemplate.query(sql, new TaskRowMapper(), tokenKey);
    }

    @Override
    public Optional<Task> findById(String tokenKey, String id) {
        String sql = """
            SELECT id, token_key, title, status, priority, due_at, created_at, updated_at, completed_at
            FROM tasks
            WHERE token_key = ? AND id = ?
            """;
        List<Task> results = jdbcTemplate.query(sql, new TaskRowMapper(), tokenKey, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Task save(String tokenKey, Task task) {
        String mergeSql = """
            INSERT INTO tasks (id, token_key, title, status, priority, due_at, created_at, updated_at, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(token_key, id) DO UPDATE SET
                title = excluded.title,
                status = excluded.status,
                priority = excluded.priority,
                due_at = excluded.due_at,
                updated_at = excluded.updated_at,
                completed_at = excluded.completed_at
            """;
        jdbcTemplate.update(mergeSql,
                task.getId(),
                tokenKey,
                task.getTitle(),
                task.getStatus().name(),
                task.getPriority().name(),
                formatDateTime(task.getDueAt()),
                formatDateTime(task.getCreatedAt()),
                formatDateTime(task.getUpdatedAt()),
                formatDateTime(task.getCompletedAt()));
        return task;
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO_FORMATTER);
    }

    private OffsetDateTime parseDateTime(String text) {
        return text == null ? null : OffsetDateTime.parse(text, ISO_FORMATTER);
    }

    private class TaskRowMapper implements RowMapper<Task> {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getString("id"));
            task.setTitle(rs.getString("title"));
            task.setStatus(TaskStatus.valueOf(rs.getString("status")));
            task.setPriority(TaskPriority.valueOf(rs.getString("priority")));
            task.setDueAt(parseDateTime(rs.getString("due_at")));
            task.setCreatedAt(parseDateTime(rs.getString("created_at")));
            task.setUpdatedAt(parseDateTime(rs.getString("updated_at")));
            task.setCompletedAt(parseDateTime(rs.getString("completed_at")));
            return task;
        }
    }
}
