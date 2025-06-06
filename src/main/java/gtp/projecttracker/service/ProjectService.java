package gtp.projecttracker.service;

import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.model.jpa.Project;
import gtp.projecttracker.repository.jpa.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Cacheable(value = "projects", key = "#id")
    public Project getProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#project.id")
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public List<Project> getProjectsWithoutTasks() {
        return projectRepository.findProjectsWithoutTasks();
    }
}
