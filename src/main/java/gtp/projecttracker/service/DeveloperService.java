package gtp.projecttracker.service;

import gtp.projecttracker.dto.request.developer.CreateDeveloperRequest;
import gtp.projecttracker.dto.request.developer.UpdateDeveloperRequest;
import gtp.projecttracker.dto.response.developer.DeveloperResponse;
import gtp.projecttracker.exception.DeveloperAlreadyExistsException;
import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.mapper.DeveloperMapper;
import gtp.projecttracker.model.jpa.Developer;
import gtp.projecttracker.repository.jpa.DeveloperRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeveloperService {
    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository, DeveloperMapper developerMapper) {
        this.developerRepository = developerRepository;
        this.developerMapper = developerMapper;
    }

    @Cacheable(value = "developers", key = "#id")
    public DeveloperResponse getDeveloperById(UUID id) {
        Developer developer = developerRepository.findDeveloperById(id);
        return developerMapper.toResponse(developer);
    }


    public Developer getDeveloperEntityById(UUID id) {
        return developerRepository.findDeveloperById(id);
    }

    public Page<DeveloperResponse> getAllDevelopers(Pageable pageable) {
        Page<Developer> developers = developerRepository.findAll(pageable);
        List<DeveloperResponse> responses = developerMapper.toResponseList(developers.getContent());
        return new PageImpl<>(responses, pageable, developers.getTotalElements());
    }

    public Optional<DeveloperResponse> getDeveloperByTaskId(UUID taskId) {
        return developerRepository.findDeveloperByTaskId(taskId)
                .map(developerMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#result.id")
    public DeveloperResponse createDeveloper(CreateDeveloperRequest request) {
        Developer dev = developerRepository.findByEmail(request.email());
        if (dev != null) {
            throw new DeveloperAlreadyExistsException(request.email());
        }

        Developer developer = developerMapper.toEntity(request);
        Developer savedDeveloper = developerRepository.save(developer);
        return developerMapper.toResponse(savedDeveloper);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#id")
    public DeveloperResponse updateDeveloper(UUID id, UpdateDeveloperRequest request) {
        Developer developer = developerRepository.findDeveloperById(id);

        developerMapper.updateEntity(developer, request);
        Developer updatedDeveloper = developerRepository.save(developer);
        return developerMapper.toResponse(updatedDeveloper);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#id")
    public DeveloperResponse patchDeveloper(UUID id, UpdateDeveloperRequest patchRequest) {
        Developer developer = developerRepository.findDeveloperById(id);

        developerMapper.updateEntity(developer, patchRequest);

        Developer updatedDeveloper = developerRepository.save(developer);
        return developerMapper.toResponse(updatedDeveloper);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#id")
    public void deleteDeveloper(UUID id) {
        if (!existsById(id)) {
            throw new ResourceNotFoundException("Developer not found with id: " + id);
        }
        developerRepository.deleteById(id);
    }

    public List<DeveloperResponse> getDevelopersBySkill(String skill) {
        List<Developer> developers = developerRepository.findBySkillsContaining(skill);
        return developerMapper.toResponseList(developers);
    }

    @Transactional
    @CacheEvict(value = "developers", key = "#id")
    public DeveloperResponse updateDeveloperSkills(UUID id, List<String> skills) {
        Developer developer = developerRepository.findDeveloperById(id);
        developer.getSkills().clear();
        developer.getSkills().addAll(skills);
        Developer updatedDeveloper = developerRepository.save(developer);
        return developerMapper.toResponse(updatedDeveloper);
    }

    public List<DeveloperResponse> getTopDevelopersByTaskCount(Pageable pageable) {
        List<Object[]> results = developerRepository.findTopDevelopersByTaskCount(pageable);
        return results.stream()
                .map(result -> (Developer) result[0])
                .map(developerMapper::toResponse)
                .toList();
    }

    public Page<DeveloperResponse> searchDevelopers(String name, String email, String skill, Pageable pageable) {
        if (name == null && email == null && skill == null) {
            return getAllDevelopers(pageable);
        }

        Page<Developer> developers;
        if (skill != null) {
            List<Developer> devsBySkill = developerRepository.findBySkillsContaining(skill);
            developers = new PageImpl<>(devsBySkill, pageable, devsBySkill.size());
        } else {
            developers = developerRepository.searchDevelopers(name, email, pageable);
        }

        List<DeveloperResponse> responses = developerMapper.toResponseList(developers.getContent());
        return new PageImpl<>(responses, pageable, developers.getTotalElements());
    }

    public boolean existsById(UUID developerId) {
        return developerId != null && developerRepository.existsById(developerId);
    }
}
