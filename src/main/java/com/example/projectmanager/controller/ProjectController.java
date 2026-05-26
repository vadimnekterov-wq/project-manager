package com.example.projectmanager.controller;

import com.example.projectmanager.model.Project;
import com.example.projectmanager.service.ProjectService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("");
            model.addAttribute("role", role);
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("projects", projectService.getAll());
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
    public String create(@ModelAttribute Project project) {
        project.setOwnerId(1L);
        projectService.create(project);
        return "redirect:/projects";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("");
            model.addAttribute("role", role);
            model.addAttribute("username", authentication.getName());
        }
        projectService.getById(id).ifPresent(p -> model.addAttribute("project", p));
        return "project-detail";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        projectService.delete(id);
        return "redirect:/projects";
    }
}