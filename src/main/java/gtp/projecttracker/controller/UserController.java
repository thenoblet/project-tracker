package gtp.projecttracker.controller;

import gtp.projecttracker.dto.response.task.TaskSummaryResponse;
import gtp.projecttracker.dto.response.user.UserResponse;
import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.service.TaskService;
import gtp.projecttracker.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for user-related operations and user management.
 * <p>
 * Provides endpoints for retrieving user information, with different access levels
 * based on user roles. Serves as the primary interface for user data retrieval operations.
 * </p>
 *
 * <p><b>Endpoint Structure:</b></p>
 * <ul>
 *   <li>Base path: {@code /api/v1/users}</li>
 *   <li>Personal user data: {@code /me}</li>
 *   <li>Admin user management: {@code /admin/users}</li>
 * </ul>
 *
 * <p><b>Security:</b></p>
 * <ul>
 *   <li>All endpoints require authentication</li>
 *   <li>Admin-specific endpoints require elevated privileges</li>
 *   <li>Uses Spring Security's {@code @PreAuthorize} for method-level security</li>
 * </ul>
 *
 * <p><b>Pagination:</b></p>
 * Admin endpoints support pagination with default page size of 10 items.
 *
 * @see UserService
 * @see UserResponse
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final TaskService taskService;

    /**
     * Constructs a new UserController with the required service dependency.
     *
     * @param userService the user service implementation for business logic operations
     */
    @Autowired
    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    /**
     * Retrieves the currently authenticated user's information.
     * <p>
     * Returns a detailed response containing the user's profile data. The information
     * returned is always specific to the currently logged-in user.
     * </p>
     *
     * @return ResponseEntity containing the user's data wrapped in {@link UserResponse}
     * @throws org.springframework.security.access.AccessDeniedException if not authenticated
     * @see UserResponse
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(new UserResponse(user));
    }

    /**
     * Retrieves a paginated list of all users (Admin-only endpoint).
     * <p>
     * This endpoint requires ADMIN or MANAGER privileges and returns a paginated
     * list of all users in the system. The default page size is 10 items,
     * which can be customized via request parameters.
     * </p>
     *
     * @param pageable the pagination information (page number, size, and sorting)
     * @return ResponseEntity containing a page of {@link UserResponse} objects
     * @throws org.springframework.security.access.AccessDeniedException if not authorized
     * @see Page
     * @see Pageable
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or @securityUtil.isTaskOwner(#id)")
    public ResponseEntity<Page<TaskSummaryResponse>> getAssignedTasks(@PathVariable UUID id, @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByUserId(id, pageable));
    }
}