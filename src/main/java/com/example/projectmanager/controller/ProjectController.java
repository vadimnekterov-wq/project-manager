package com.example.projectmanager.controller;

import com.example.projectmanager.model.Project;
import com.example.projectmanager.repository.UserRepository;
import com.example.projectmanager.service.ProjectService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model, Authentication auth) {
        model.addAttribute("projects", projectService.getAll());
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("isManager", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
        return "projects";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        return "project-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String create(@ModelAttribute Project project, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName()).get().getId();
        project.setOwnerId(userId);
        projectService.create(project);
        return "redirect:/projects";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String delete(@PathVariable Long id) {
        projectService.delete(id);
        return "redirect:/projects";
    }
}
