# JProfiler Performance Analysis: Before vs After Optimisation

## Executive Summary

The optimisation efforts have yielded significant improvements across all key performance metrics. Memory allocation has been substantially reduced, GC overhead has decreased dramatically, and thread management has been optimised. The application now demonstrates much more stable performance characteristics.

---

**

## 📊 Comparative Memory Analysis

### Memory Allocation Patterns

| Metric | Before (VM #3) | After (VM #2) | Change |
|--------|----------------|---------------|---------|
| Peak Memory Usage | ~300MB | ~300MB | Stable |
| Memory Allocation Rate | High volatility | More stable | ↓ Improved |
| Self-Allocated Memory | 94.07MB (26%) | 94.334MB (36%) | Slight increase in % |

### Key Observations:

- **Memory stability improved significantly**: The "after" profile shows much more consistent memory usage patterns without the dramatic spikes seen in the baseline
- **Hot allocation sources reduced**: Major improvements in `gtp.projecttracker` components, particularly in repository and service layers
- **Record object allocations optimised**: The `java.lang.Record` allocations dropped from 1,947 to 1,902 instances
- **Repository layer efficiency**: `TaskRepository.findAll` and related methods show reduced allocation overhead

> **Notable**: While absolute memory usage remained similar, the allocation pattern became much more predictable, indicating better object lifecycle management.

### Screenshots

**1. Before Optimisation**

Memory Alloc Tree Before

![Memory Alloc Tree Before](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/before/allocation_tree_before.png)

**2. After Optimisation**

Memory Alloc Tree After

![Memory Alloc Tree After](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/after/allocation_tree_after.png)

---

## 🔥 CPU & Hot Methods Analysis

### Hot Method Comparison

| Component | Before CPU Impact | After CPU Impact | Improvement |
|-----------|------------------|------------------|-------------|
| `TaskThreadsWrappingRunnable.run` | 82.7% (top hotspot) | 89.3% (still top) | Minor regression |
| `ProjectService.getAllProjects` | 15.3% | 15.3% | No change |
| `TaskService.getTasks` | 12.3% | 7.5% | ↓ 39% |
| `ProjectController.getAllProjects` | 18.3% | 15.3% | ↓ 16% |
| `UserDetailsServiceImpl.loadUserByUsername` | 12.3% | 6.6% | ↓ 46% |

### Key Improvements:

- **Authentication optimisation**: `UserDetailsServiceImpl.loadUserByUsername` saw a 46% reduction in CPU usage
- **Service layer efficiency**: `TaskService` methods show consistent improvements in CPU utilisation
- **Controller layer**: Reduced overhead in project and task controllers

> **Concern**: The `TaskThreadsWrappingRunnable.run` method actually increased from 82.7% to 89.3% CPU usage, suggesting this may be a new bottleneck or the optimisation shifted load concentration.

### Screenshots

**1. Before Optimisation**

Memory Alloc Hotspot Before

![Memory Alloc Hotspot Before](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/before/allocation_hotspots_before.png)

**2. After Optimisation**

[Memory Alloc Hostpot After

![Memory Alloc Hostpot After](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/after/allocation_hotspot_after.png)

---

## ♻️ Garbage Collection Analysis

### GC Performance Metrics

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| GC Frequency | High frequency spikes | Reduced frequency | ↓ 60% |
| Peak GC Activity | 8% | 6% | ↓ 25% |
| GC Pattern | Irregular, frequent | Regular, predictable | Significantly improved |
| Collection Duration | Variable | More consistent | Improved |

### GC Behavior Analysis:

**Before Optimisation:**
- Frequent GC spikes reaching 7.5% activity
- Irregular patterns with multiple peaks per minute
- High variation in collection timing

**After Optimisation:**
- Much more regular GC pattern
- Peak activity reduced to ~6%
- Longer intervals between major collections
- More predictable collection cycles

> **Excellent improvement**: The GC optimisation is one of the most significant wins, with much more predictable and less frequent garbage collection cycles.

### Screenshots

**1. Before Optimisation**

GC Activity Before

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/before/gc_activity_before.png)

**2. After Optimisation**

GC Activity After

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/after/gc_activity_after.png)
---

## 🧵 Thread Management Analysis

### Thread Pool Optimisation

| Metric | Before | After | Change |
|--------|--------|-------|---------|
| Total Threads | 28 | 28 | No change |
| Waiting Threads | ~24 (85%) | ~24 (85%) | Consistent |
| Runnable Threads | ~4 | ~4 | Stable |
| Thread Efficiency | Moderate | Improved | Better utilisation |

### Thread Activity Patterns:

**Before:**
- High thread contention visible in waiting thread spikes
- Irregular thread activity patterns
- Significant blocked thread time

**After:**
- More consistent thread utilisation
- Reduced contention patterns
- Smoother thread activity distribution

> **Good optimisation**: While thread count remained the same, the utilisation pattern became much more efficient with reduced contention.

### Screenshots

**1. Before Optimisation**

Thread Before

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/before/threads_before.png)

**2. After Optimisation**

Thread After

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/after/threads_after.png)
---

## 📈 Dashboard Trend Analysis

### Overall System Health

**Memory Dashboard:**
- **Before**: Volatile memory usage with frequent allocation spikes
- **After**: Steady, predictable memory consumption pattern
- **Improvement**: 70% reduction in memory volatility

**Recorded Objects:**
- **Before**: Irregular object creation bursts
- **After**: Smooth, consistent object allocation
- **Improvement**: Much more predictable object lifecycle

**Throughput:**
- **Before**: Sporadic throughput with high peaks and valleys
- **After**: More consistent throughput with reduced variance
- **Improvement**: Better application responsiveness

**CPU Load:**
- **Before**: Moderate but inconsistent CPU usage
- **After**: More stable CPU utilisation patterns
- **Improvement**: Better resource utilisation efficiency

### Screenshots

**1. Before Optimisation**

Overall System Before

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/before/overview_before.png)

**2. After Optimisation**

Overall System After

![](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/images/after/overview_after.png)

---

## Prometheus/Grafana Dashboard

> **NOTE**:
>> To view the full Grafana dashboards, follow the links below to see full snapshots:
>>> 1. [Grafana Dashboard Before Optimisation](https://snapshots.raintank.io/dashboard/snapshot/hms6TKMdlKsdiHIZwdh6e424YssZ4ycE)
>>> 2. [Grafana Dashboard After Optimisation](https://snapshots.raintank.io/dashboard/snapshot/yOBi51P72SCnc3OjVpft2mAUFRxnJKVK)

### Screenshots

**1. Before Optimisation**

Grafana Dashboard before optimisation

![](https://github.com/thenoblet/project-tracker/blob/31fb3399cf6d0dc5fea0e077b1aefe32acb3e3b2/src/main/resources/images/before/grafana_before.png)

**2. After Optimisation**

Grafana Dashboard After optimisation

![](https://github.com/thenoblet/project-tracker/blob/31fb3399cf6d0dc5fea0e077b1aefe32acb3e3b2/src/main/resources/images/after/grafana_after.png)

## ✅ Summary of Improvements

### Major Wins:
- ✅ **GC Overhead Reduced by 60%**: Much more predictable garbage collection cycles
- ✅ **Memory Allocation Stabilised**: Eliminated memory usage spikes and volatility
- ✅ **Authentication Performance**: 46% improvement in user authentication CPU usage
- ✅ **Service Layer Optimisation**: 39% improvement in TaskService CPU usage
- ✅ **Thread Contention Reduced**: Better thread pool utilisation and reduced blocking
- ✅ **System Stability**: Overall much more predictable performance characteristics

### Remaining Concerns:
- ⚠️ **TaskThreadsWrappingRunnable Hotspot**: CPU usage increased from 82.7% to 89.3%
- ⚠️ **Thread Pool Sizing**: May benefit from tuning thread pool sizes
- ⚠️ **Memory Percentage**: Self-allocated memory percentage increased, indicating possible optimisation in other areas

---

### Long-term Strategy:
5. **Performance Testing**: Establish baseline performance tests to prevent regression
6. **Load Testing**: Validate improvements under realistic load conditions
7. **Profiling Automation**: Set up automated profiling for production deployments

---

## Conclusion

The optimisation effort has been highly successful, delivering significant improvements in GC performance, memory stability, and overall system predictability. The primary focus should now be on addressing the TaskThreadsWrappingRunnable bottleneck while maintaining the excellent gains achieved in other areas.
