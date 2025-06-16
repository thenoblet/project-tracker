Here's a comprehensive endpoints table extracted from your OpenAPI specification:

## API Endpoints Reference

### Projects

| Method | Endpoint                          | Description                                  | Parameters/Request Body                     |
|--------|-----------------------------------|----------------------------------------------|---------------------------------------------|
| GET    | `/api/v1/projects`                | Get paginated list of projects               | `pageable`, `includeTasks` (query)          |
| POST   | `/api/v1/projects`                | Create a new project                         | `CreateProjectRequest` (body)               |
| GET    | `/api/v1/projects/{id}`           | Get project by ID                            | `id` (path)                                 |
| PUT    | `/api/v1/projects/{id}`           | Update entire project                        | `id` (path), `UpdateProjectRequest` (body)  |
| PATCH  | `/api/v1/projects/{id}`           | Partial update of project                    | `id` (path), `UpdateProjectRequest` (body)  |
| DELETE | `/api/v1/projects/{id}`           | Delete project                               | `id` (path)                                 |
| PATCH  | `/api/v1/projects/{id}/status`    | Update project status                        | `id` (path), `status` (query)               |
| GET    | `/api/v1/projects/{id}/tasks`     | Get tasks for project                        | `id` (path), filters (query), `pageable`    |
| GET    | `/api/v1/projects/{id}/tasks/overdue` | Get overdue tasks for project             | `id` (path), `pageable`                     |

### Tasks

| Method | Endpoint                          | Description                                  | Parameters/Request Body                     |
|--------|-----------------------------------|----------------------------------------------|---------------------------------------------|
| GET    | `/api/v1/tasks`                   | Get paginated list of all tasks              | `pageable` (query)                          |
| POST   | `/api/v1/tasks`                   | Create a new task                            | `CreateTaskRequest` (body)                  |
| GET    | `/api/v1/tasks/{id}`              | Get task by ID                               | `id` (path)                                 |
| PUT    | `/api/v1/tasks/{id}`              | Update entire task                           | `id` (path), `UpdateTaskRequest` (body)     |
| PATCH  | `/api/v1/tasks/{id}`              | Partial update of task                       | `id` (path), `UpdateTaskRequest` (body)     |
| DELETE | `/api/v1/tasks/{id}`              | Delete task                                  | `id` (path)                                 |
| POST   | `/api/v1/tasks/{id}/assign`       | Assign task to developer                     | `id` (path), `AssignTaskRequest` (body)     |
| GET    | `/api/v1/tasks/overdue`           | Get all overdue tasks                        | `pageable` (query)                          |
| GET    | `/api/v1/tasks/test-publish`      | Test endpoint                                | -                                           |

### Developers

| Method | Endpoint                          | Description                                  | Parameters/Request Body                     |
|--------|-----------------------------------|----------------------------------------------|---------------------------------------------|
| GET    | `/api/v1/developers`              | Get paginated list of developers             | `pageable` (query)                          |
| POST   | `/api/v1/developers`              | Create new developer                         | `CreateDeveloperRequest` (body)             |
| GET    | `/api/v1/developers/{id}`         | Get developer by ID                          | `id` (path)                                 |
| PUT    | `/api/v1/developers/{id}`         | Update entire developer                      | `id` (path), `UpdateDeveloperRequest` (body)|
| PATCH  | `/api/v1/developers/{id}`         | Partial update of developer                  | `id` (path), `UpdateDeveloperRequest` (body)|
| DELETE | `/api/v1/developers/{id}`         | Delete developer                             | `id` (path)                                 |
| GET    | `/api/v1/developers/top`          | Get top developers                           | `pageable` (query)                          |
| GET    | `/api/v1/developers/search`       | Search developers                            | `name`, `email`, `skill` (query), `pageable`|

### Audit Logs

| Method | Endpoint                          | Description                                  | Parameters/Request Body                     |
|--------|-----------------------------------|----------------------------------------------|---------------------------------------------|
| GET    | `/api/audit-logs`                 | Get paginated audit logs                     | `pageable` (query)                          |
| GET    | `/api/audit-logs/entity/{entityType}` | Get logs by entity type                  | `entityType` (path)                         |
| GET    | `/api/audit-logs/date-range`      | Get logs within date range                   | `start`, `end` (query - date-time)          |
| GET    | `/api/audit-logs/actor/{actorName}` | Get logs by actor name                    | `actorName` (path)                          |

### Other

| Method | Endpoint                          | Description                                  | Parameters/Request Body                     |
|--------|-----------------------------------|----------------------------------------------|---------------------------------------------|
| POST   | `/api/v1/email/send`              | Send test email                              | `recipient` (query), optional body          |

## Common Parameters

- `pageable`: Pagination parameters (page, size, sort)
- `id`: UUID format (path parameter)
- Status enums:
    - Project: ACTIVE, IN_PROGRESS, BLOCKED, ON_HOLD, IN_REVIEW, COMPLETED, CANCELLED
    - Task: TODO, ASSIGNED, APPROVED, IN_PROGRESS, DONE, BLOCKED