package gtp.projecttracker.service;

import gtp.projecttracker.model.jpa.Developer;
import gtp.projecttracker.repository.jpa.DeveloperRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeveloperService {
    private final DeveloperRepository developerRepository;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Cacheable(value = "developers", key = "#id")
    public Developer getDeveloperById(UUID id) {
        return developerRepository.findById(id);
    }

    public Page<Developer> getAllDevelopers(Pageable pageable) {
        return developerRepository.findAll(pageable);
    }

    public Optional<Developer> getDeveloperByTaskId(Long taskId) {
        return developerRepository.findDeveloperByTaskId(taskId);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#developer.id")
    public Developer saveDeveloper(Developer developer) {
        return developerRepository.save(developer);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#id")
    public void deleteDeveloper(Long id) {
        developerRepository.deleteById(id);
    }

    public List<Developer> getDevelopersBySkill(String skill) {
        return developerRepository.findBySkillsContaining(skill);
    }

    public Developer updateDeveloperSkills(UUID id, List<String> skills) {
        Developer developer = getDeveloperById(id);
        developer.getSkills().clear();
        developer.getSkills().addAll(skills);
        return saveDeveloper(developer);
    }

    public List<Developer> getTopDevelopersByTaskCount(Pageable pageable) {
        List<Object[]> results = developerRepository.findTopDevelopersByTaskCount(pageable);
        return results.stream()
                .map(result -> (Developer) result[0])
                .toList();
    }
}
