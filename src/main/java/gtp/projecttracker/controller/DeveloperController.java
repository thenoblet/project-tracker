package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.developer.CreateDeveloperRequest;
import gtp.projecttracker.dto.request.developer.UpdateDeveloperRequest;
import gtp.projecttracker.dto.response.developer.DeveloperResponse;
import gtp.projecttracker.service.DeveloperService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {
    private final DeveloperService developerService;

    public DeveloperController(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @PostMapping
    public ResponseEntity<DeveloperResponse> createDeveloper(
            @Valid @RequestBody CreateDeveloperRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(developerService.createDeveloper(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeveloperResponse> getDeveloperById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(developerService.getDeveloperById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DeveloperResponse>> getAllDevelopers(
            Pageable pageable) {
        return ResponseEntity.ok(developerService.getAllDevelopers(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeveloperResponse> updateDeveloper(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeveloperRequest request) {
        return ResponseEntity.ok(developerService.updateDeveloper(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeveloperResponse> patchDeveloper(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeveloperRequest request
    ) {
        return ResponseEntity.ok(developerService.patchDeveloper(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(
            @PathVariable UUID id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DeveloperResponse>> searchDevelopers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String skill,
            Pageable pageable) {
        Page<DeveloperResponse> developers = developerService.searchDevelopers(name, email, skill, pageable);
        return ResponseEntity.ok(developers);
    }

    @GetMapping("/top")
    public ResponseEntity<List<DeveloperResponse>> getTopDevelopers(Pageable pageable) {
        return ResponseEntity.ok(developerService.getTopDevelopersByTaskCount(pageable));
    }
}