package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.developer.CreateDeveloperRequest;
import gtp.projecttracker.dto.response.developer.DeveloperResponse;
import gtp.projecttracker.service.DeveloperService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(developerService.createDeveloper(request));
    }

}
