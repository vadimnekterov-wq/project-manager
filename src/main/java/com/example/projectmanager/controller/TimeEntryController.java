package com.example.projectmanager.controller;

import com.example.projectmanager.model.Task;
import com.example.projectmanager.model.TimeEntry;
import com.example.projectmanager.repository.TaskRepository;
import com.example.projectmanager.repository.TimeEntryRepository;
import com.example.projectmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/time")
public class TimeEntryController {
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/task/{taskId}")
    public String list(@PathVariable Long taskId, Model model) {
        model.addAttribute("entries", timeEntryRepository.findByTaskId(taskId));
        model.addAttribute("taskId", taskId);
        model.addAttribute("totalMinutes", timeEntryRepository.getTotalMinutesByTask(taskId));
        return "time-entries";
    }

    @PostMapping("/start")
    public String start(@RequestParam Long taskId, Authentication auth) {
        Task task = taskRepository.findById(taskId);
        if (task == null) {
            return "redirect:/time/task/" + taskId;
        }

        Long currentUserId = userRepository.findByUsername(auth.getName()).get().getId();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdmin && !isManager && !currentUserId.equals(task.getAssigneeId())) {
            return "redirect:/time/task/" + taskId;
        }

        TimeEntry entry = new TimeEntry();
        entry.setTaskId(taskId);
        entry.setUserId(currentUserId);
        entry.setStartTime(LocalDateTime.now());
        timeEntryRepository.save(entry);
        return "redirect:/time/task/" + taskId;
    }

    @GetMapping("/stop/{id}/{taskId}")
    public String stop(@PathVariable Long id, @PathVariable Long taskId) {
        TimeEntry entry = timeEntryRepository.findById(id);
        if (entry == null) {
            return "redirect:/time/task/" + taskId;
        }
        entry.setEndTime(LocalDateTime.now());
        timeEntryRepository.save(entry);
        return "redirect:/time/task/" + taskId;
    }
}
