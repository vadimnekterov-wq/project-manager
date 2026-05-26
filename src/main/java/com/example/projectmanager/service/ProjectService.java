package com.example.projectmanager.service;

import com.example.projectmanager.model.Project;
import com.example.projectmanager.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project create(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAll() {
        return (List<Project>) projectRepository.findAll();
    }

    public List<Project> getByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    public List<Project> getByMember(Long userId) {
        return projectRepository.findByMemberId(userId);
    }

    public Optional<Project> getById(Long id) {
        return projectRepository.findById(id);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}