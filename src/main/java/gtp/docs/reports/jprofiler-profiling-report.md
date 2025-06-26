# 🚀 Performance Profiling Report
**Application**: Project Tracker API  
**Test Scenario**: 200-user load test on `/api/v1/tasks`, `/api/v1/projects`, and `/api/v1/users`

---

## 📊 1. Memory Allocation Analysis
![Memory Allocation](jetbrains://idea/navigate/reference?project=project-tracker&path=images%2Fmemory_allocation.png)

### Key Findings
- **Top Allocators**:
    - `ThreadPoolExecutor$Worker.run()`: **60% of allocations** (42KB)
    - `JwtAuthFilter.doFilterInternal()`: **17.1%** (10.5KB) tied to `/api/v1/projects`
    - `ProjectController.getAllProjects()`: **13.1%** (8KB)
- **Patterns**:
    - Repeated allocations in JWT filtering and project/task fetching.
    - High churn from `UserDetailsServiceImpl.loadUserByUsername()` (1% of allocations).

### Recommendations
✅ **Optimize JWT Filter**: Cache parsed tokens to reduce `byte[]` allocations.  
✅ **DTO Projections**: Replace entity returns with DTOs in `getAllProjects()`/`getAllTasks()`.  
✅ **User Caching**: Cache `UserRepository.findByEmail()` results (called 20+ times).

---

## 🧵 2. Thread Dump Analysis
![Thread Dump](jetbrains://idea/navigate/reference?project=project-tracker&path=images%2Fthread_dump.png)

### Key Findings
- **Thread States**:
    - 10+ `http-nio-8080-exec-*` threads in `RUNNABLE` state.
    - `TaskThread$WrappingRunnable` blocked on `LockSupport.park()` (thread pool exhaustion).
- **Bottlenecks**:
    - Thread contention in `TaskRepository.findOverdueTasks()` (30 invocations).

### Recommendations
✅ **Increase Thread Pool**: Adjust Tomcat's `maxThreads` (default 200 → 250).  
✅ **Async Processing**: Offload `findOverdueTasks()` to a scheduled async job.  
✅ **Connection Pooling**: Verify PostgreSQL/MongoDB connection pool settings.

---

## ♻️ 3. Garbage Collection Behavior
![GC Pauses](jetbrains://idea/navigate/reference?project=project-tracker&path=images%2Fgc_pauses.png)

### Key Findings
- **GC Activity**: Minimal Full GCs (healthy), but frequent minor GCs.
- **Heap Behavior**:
    - Steady allocation rate (no leaks detected).
    - Old Gen peaked at **85% usage** during load.

### Recommendations
✅ **Increase Heap**: Set `-Xmx` to 4GB (from current 2GB).  
✅ **G1GC Tuning**: Add `-XX:MaxGCPauseMillis=200` to prioritize latency.

---

## 🔥 4. CPU Hotspots
**Top Methods ![CPU Hotspots](jetbrains://idea/navigate/reference?project=project-tracker&path=images%2Fcpu_hotspots.png)**:  
| Method                                      | Self Time (ms) | Invocations |  
|---------------------------------------------|----------------|-------------|  
| `TaskRepository.findOverdueTasks()`         | 749,056        | 30          |  
| `UserRepository.findByEmail()`              | 212,536        | 20          |  
| `JwtParserBuilder.build()`                  | 170,113        | 40          |

### Key Findings
- **JWT Overhead**: Token parsing (`JwtParserBuilder`) consumed **170ms CPU time**.
- **DB Bottlenecks**:
    - `findOverdueTasks()` took **249ms/query** (needs indexing).
    - `UserRepository.findByEmail()` averaged **10.6ms/query**.

### Recommendations
✅ **JWT Optimization**: Pre-compile `JwtParserBuilder` at startup.  
✅ **Database Indexes**: Add index on `Task.dueDate` and `User.email`.  
✅ **Batch Processing**: Combine `findOverdueTasks()` checks into a single query.

---

## 📎 Attachments
- [Full JProfiler Snapshot](jetbrains://idea/navigate/reference?project=project-tracker&path=shots%2Fsnapshots%2Fjprofiler_snapshot.jps)
- [JMeter Test Plan](jetbrains://idea/navigate/reference?project=project-tracker&path=shots%2Ftest_plans%2Fjmeter_testplan.jmx)

---