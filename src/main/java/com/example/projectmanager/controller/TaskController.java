package com.example.projectmanager.controller;

import com.example.projectmanager.model.Task;
import com.example.projectmanager.repository.TaskRepository;
import com.example.projectmanager.repository.TimeEntryRepository;
import com.example.projectmanager.repository.UserRepository;
import com.example.projectmanager.service.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TimeEntryRepository timeEntryRepository;

    public TaskController(TaskRepository taskRepository,
                          UserRepository userRepository,
                          EmailService emailService,
                          TimeEntryRepository timeEntryRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.timeEntryRepository = timeEntryRepository;
    }

    @GetMapping("/project/{projectId}")
    public String list(@PathVariable Long projectId, Model model, Authentication auth) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        Map<Long, String> assigneeNames = new HashMap<>();
        for (Task t : tasks) {
            if (t.getAssigneeId() != null && !assigneeNames.containsKey(t.getAssigneeId())) {
                userRepository.findById(t.getAssigneeId()).ifPresent(u -> assigneeNames.put(u.getId(), u.getUsername()));
            }
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("assigneeNames", assigneeNames);
        model.addAttribute("projectId", projectId);
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("isManager", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
        Long currentUserId = userRepository.findByUsername(auth.getName()).get().getId();
        model.addAttribute("currentUserId", currentUserId);
        return "tasks";
    }

    @GetMapping("/new/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createForm(@PathVariable Long projectId, Model model) {
        Task task = new Task();
        task.setProjectId(projectId);
        model.addAttribute("task", task);
        model.addAttribute("users", userRepository.findAll());
        return "task-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String create(@ModelAttribute Task task, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName()).get().getId();
        task.setCreatorId(userId);
        taskRepository.save(task);
        if (task.getAssigneeId() != null) {
            userRepository.findById(task.getAssigneeId()).ifPresent(u ->
                    emailService.sendTaskAssigned(u.getEmail(), task.getTitle()));
        }
        return "redirect:/tasks/project/" + task.getProjectId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id);
        model.addAttribute("task", task);
        model.addAttribute("users", userRepository.findAll());
        return "task-edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String update(@PathVariable Long id, @ModelAttribute Task task) {
        Task existing = taskRepository.findById(id);
        if (existing == null) {
            return "redirect:/projects";
        }
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setPriority(task.getPriority());
        existing.setAssigneeId(task.getAssigneeId());
        existing.setDueDate(task.getDueDate());
        taskRepository.save(existing);
        return "redirect:/tasks/project/" + existing.getProjectId();
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status,
                               @RequestParam Long projectId, Authentication auth) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            return "redirect:/tasks/project/" + projectId;
        }
        Long currentUserId = userRepository.findByUsername(auth.getName()).get().getId();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdmin && !isManager && !currentUserId.equals(task.getAssigneeId())) {
            return "redirect:/tasks/project/" + projectId;
        }

        task.setStatus(status);
        taskRepository.save(task);
        userRepository.findById(task.getCreatorId()).ifPresent(u ->
                emailService.sendTaskStatusChanged(u.getEmail(), task.getTitle(), status));
        return "redirect:/tasks/project/" + projectId;
    }

    @GetMapping("/{id}/delete/{projectId}")
    public String delete(@PathVariable Long id, @PathVariable Long projectId) {
        taskRepository.deleteById(id);
        return "redirect:/tasks/project/" + projectId;
    }

    @GetMapping("/statistics/{projectId}")
    public String statistics(@PathVariable Long projectId, Model model) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        long total = tasks.size();
        long newTasks = tasks.stream().filter(t -> "NEW".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        Long totalSeconds = 0L;
        for (Task task : tasks) {
            Long taskMinutes = timeEntryRepository.getTotalMinutesByTask(task.getId());
            if (taskMinutes != null) totalSeconds += taskMinutes * 60;
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        model.addAttribute("projectId", projectId);
        model.addAttribute("totalTasks", total);
        model.addAttribute("newTasks", newTasks);
        model.addAttribute("inProgressTasks", inProgress);
        model.addAttribute("doneTasks", done);
        model.addAttribute("hours", hours);
        model.addAttribute("minutes", minutes);
        return "statistics";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model, Authentication auth) {
        List<Task> tasks = taskRepository.search(query);
        model.addAttribute("tasks", tasks);
        model.addAttribute("query", query);
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("isManager", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
        if (!tasks.isEmpty()) {
            model.addAttribute("projectId", tasks.get(0).getProjectId());
        }
        return "tasks";
    }
}
