package com.example.projectmanager.service;

import com.example.projectmanager.model.TimeEntry;
import com.example.projectmanager.repository.TimeEntryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    public TimeEntry startTimer(TimeEntry timeEntry) {
        return timeEntryRepository.save(timeEntry);
    }

    public TimeEntry stopTimer(Long id) {
        TimeEntry entry = timeEntryRepository.findById(id).orElseThrow();
        entry.setEndTime(java.time.LocalDateTime.now());
        return timeEntryRepository.save(entry);
    }

    public List<TimeEntry> getByTask(Long taskId) {
        return timeEntryRepository.findByTaskId(taskId);
    }

    public Long getTotalMinutes(Long taskId) {
        return timeEntryRepository.getTotalMinutesByTask(taskId);
    }
}