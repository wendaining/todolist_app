package com.todolist.api.task.integration;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.repository.JdbcTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for JdbcTaskRepository.
 * Verifies that task data survives repository recreation (simulating service restart).
 */
class TaskPersistenceIntegrationTest {

    private JdbcTemplate jdbcTemplate;
    private SingleConnectionDataSource dataSource;

    @BeforeEach
    void setUp() {
        // Use SingleConnectionDataSource to keep the same in-memory DB across operations
        dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite::memory:");
        dataSource.setSuppressClose(true);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // Create table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS tasks (
                id TEXT NOT NULL,
                token_key TEXT NOT NULL,
                title TEXT NOT NULL,
                status TEXT NOT NULL,
                priority TEXT NOT NULL,
                due_at TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                completed_at TEXT,
                PRIMARY KEY (token_key, id)
            )
            """);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.destroy();
        }
    }

    @Test
    void taskSurvivesRepositoryRecreation() {
        String tokenKey = "test-token";

        // First repository instance: save a task
        JdbcTaskRepository repo1 = new JdbcTaskRepository(jdbcTemplate);
        Task task = Task.createNew("task-1", "Persist me", TaskPriority.HIGH, null);
        repo1.save(tokenKey, task);

        // Simulate service restart: create new repository instance
        JdbcTaskRepository repo2 = new JdbcTaskRepository(jdbcTemplate);

        // Verify task is still accessible
        Optional<Task> found = repo2.findById(tokenKey, "task-1");
        assertTrue(found.isPresent(), "Task should persist across repository instances");
        assertEquals("Persist me", found.get().getTitle());
        assertEquals(TaskPriority.HIGH, found.get().getPriority());
    }

    @Test
    void tasksIsolatedByToken() {
        JdbcTaskRepository repo = new JdbcTaskRepository(jdbcTemplate);

        Task task1 = Task.createNew("shared-id", "Token A task", TaskPriority.LOW, null);
        Task task2 = Task.createNew("shared-id", "Token B task", TaskPriority.HIGH, null);

        repo.save("token-a", task1);
        repo.save("token-b", task2);

        List<Task> tokenATasks = repo.findAll("token-a");
        List<Task> tokenBTasks = repo.findAll("token-b");

        assertEquals(1, tokenATasks.size());
        assertEquals(1, tokenBTasks.size());
        assertEquals("Token A task", tokenATasks.get(0).getTitle());
        assertEquals("Token B task", tokenBTasks.get(0).getTitle());
    }

    @Test
    void updateExistingTask() {
        JdbcTaskRepository repo = new JdbcTaskRepository(jdbcTemplate);
        String tokenKey = "test-token";

        Task task = Task.createNew("task-1", "Original title", TaskPriority.MEDIUM, null);
        repo.save(tokenKey, task);

        // Update the task
        task.setTitle("Updated title");
        task.setPriority(TaskPriority.HIGH);
        repo.save(tokenKey, task);

        // Verify update
        Optional<Task> found = repo.findById(tokenKey, "task-1");
        assertTrue(found.isPresent());
        assertEquals("Updated title", found.get().getTitle());
        assertEquals(TaskPriority.HIGH, found.get().getPriority());

        // Verify no duplicates
        List<Task> all = repo.findAll(tokenKey);
        assertEquals(1, all.size());
    }
}
