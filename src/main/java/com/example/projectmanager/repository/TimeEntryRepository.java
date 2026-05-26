package com.example.projectmanager.repository;

import com.example.projectmanager.model.TimeEntry;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TimeEntryRepository extends CrudRepository<TimeEntry, Long> {
    List<TimeEntry> findByTaskId(Long taskId);
    List<TimeEntry> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, start_time, COALESCE(end_time, NOW()))), 0) FROM time_entries WHERE task_id = :taskId")
    Long getTotalMinutesByTask(@Param("taskId") Long taskId);
}
