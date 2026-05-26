package com.example.projectmanager.controller;

import com.example.projectmanager.model.Task;
import com.example.projectmanager.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/project/{projectId}")
    public String list(@PathVariable Long projectId, Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("");
            model.addAttribute("role", role);
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("tasks", taskService.getByProject(projectId));
        model.addAttribute("projectId", projectId);
        return "tasks";
    }

    @GetMapping("/new/{projectId}")
    public String createForm(@PathVariable Long projectId, Model model) {
        Task task = new Task();
        task.setProjectId(projectId);
        model.addAttribute("task", task);
        return "task-form";
    }

    @PostMapping
    public String create(@ModelAttribute Task task) {
        task.setCreatorId(1L);
        taskService.create(task);
        return "redirect:/tasks/project/" + task.getProjectId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        taskService.getById(id).ifPresent(t -> model.addAttribute("task", t));
        return "task-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Task task) {
        task.setId(id);
        taskService.update(task);
        return "redirect:/tasks/project/" + task.getProjectId();
    }

    @GetMapping("/{id}/delete/{projectId}")
    public String delete(@PathVariable Long id, @PathVariable Long projectId) {
        taskService.delete(id);
        return "redirect:/tasks/project/" + projectId;
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<Task> tasks = taskService.search(query);
        model.addAttribute("tasks", tasks);
        model.addAttribute("query", query);
        if (!tasks.isEmpty()) {
            model.addAttribute("projectId", tasks.get(0).getProjectId());
        }
        return "tasks";
    }

    @GetMapping("/statistics/{projectId}")
    public String statistics(@PathVariable Long projectId, Model model) {
        List<Task> tasks = taskService.getByProject(projectId);

        long total = tasks.size();
        long newTasks = tasks.stream().filter(t -> "NEW".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();

        model.addAttribute("projectId", projectId);
        model.addAttribute("totalTasks", total);
        model.addAttribute("newTasks", newTasks);
        model.addAttribute("inProgressTasks", inProgress);
        model.addAttribute("doneTasks", done);

        return "statistics";
    }
}