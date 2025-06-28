# JMeter Performance Test Analysis Report

## Overview

This report analyses the performance of a Spring Boot application based on JMeter reports: 
- aggregate reports ([Aggregate Report Before](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/shots/snapshots/jmeter/aggregate_before.csv)) and [Aggregate Report Before](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/shots/snapshots/jmeter/aggregate_after.csv))
- and summary reports ([Summary Report Before](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/shots/snapshots/jmeter/summary_before.csv) and [Summary Report After](https://github.com/thenoblet/project-tracker/blob/4ffb1831e43a59d8a278b7837a61b525085eb034/src/main/resources/shots/snapshots/jmeter/summary_after.csv)) before and after optimisations. 

The analysis covers five endpoints: 
- `api/v1/tasks` (GET ALL TASKS)
- `api/v1/tasks` (CREATE TASK REQUEST)
- `api/v1/projects` (GET PROJECTS)
- `api/v1/users` (GET ALL USERS)
- `api/v1/users/{id}/tasks` (GET USER TASKS)
  
  with a total of 2000 samples each. 
  The tests were conducted using JMeter with **200 threads (users)**, **a ramp-up period of 60 seconds**, and a **loop count of 2**. The goal is to evaluate the impact of optimisations, compare aggregate and summary data and identify anomalies, and provide action plans for long term optimisations.
---

## Response Time Analysis

### Response Time Metrics

The table below summarises the minimum, average, median, maximum, and percentile response times (in milliseconds) from both aggregate and summary reports before and after optimisations.

| Endpoint | Min (ms) Before (Agg/Sum) | Avg (ms) Before (Agg/Sum) | Median (ms) Before (Agg) | 90% Line Before (Agg) | 95% Line Before (Agg) | 99% Line Before (Agg) | Max (ms) Before (Agg/Sum) | Min (ms) After (Agg/Sum) | Avg (ms) After (Agg/Sum) | Median (ms) After (Agg) | 90% Line After (Agg) | 95% Line After (Agg) | 99% Line After (Agg) | Max (ms) After (Agg/Sum) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| api/v1/tasks (GET ALL TASKS) | 36/36 | 125/125 | 62 | 219 | 388 | 1177 | 2280/2280 | 28/28 | 61/61 | 43 | 83 | 133 | 455 | 572/572 |
| api/v1/tasks (CREATE TASK REQUEST) | 37/37 | 91/91 | 58 | 170 | 224 | 567 | 737/737 | 23/23 | 53/53 | 45 | 71 | 89 | 300 | 511/511 |
| api/v1/projects (GET PROJECTS) | 259/259 | 534/534 | 418 | 881 | 1004 | 1322 | 2818/2818 | 23/23 | 48/48 | 40 | 59 | 72 | 281 | 566/566 |
| api/v1/users (GET ALL USERS) | 32/32 | 104/104 | 57 | 147 | 205 | 715 | 2357/2357 | 25/25 | 48/48 | 42 | 64 | 82 | 133 | 396/396 |
| api/v1/users/{id}/tasks (GET USER TASKS) | 49/49 | 117/117 | 84 | 203 | 277 | 470 | 830/830 | 26/26 | 54/54 | 46 | 72 | 94 | 166 | 540/540 |
| TOTAL | 32/32 | 194/194 | 81 | 468 | 721 | 1123 | 2818/2818 | 23/23 | 53/53 | 43 | 70 | 94 | 295 | 572/572 |

### Response Time Comparison (Aggregate vs. Summary)

- **Consistency**: Aggregate and summary reports show identical min, avg, and max values, indicating no discrepancies in basic metrics across the 200-thread, 60-second ramp-up test.
- **Added Insight from Aggregate**: The aggregate report provides median, 90th, 95th, and 99th percentiles, offering a deeper view of distribution. For example, `api/v1/projects` (GET PROJECTS)’ 95th percentile before optimisation (1004 ms) was near the 1000 ms SLA, a detail absent in the summary.
- **Interesting Fact**: The aggregate data highlights `api/v1/projects` (GET PROJECTS)’ pre-optimisation 99th percentile (1322 ms) as a critical outlier, which the summary’s max (2818 ms) alone doesn’t fully contextualise.

### Response Time Anomalies

- **Threshold Exceedance**: Before optimisations, endpoints exceeded 2000 ms:
  - `api/v1/tasks` (GET ALL TASKS): Max 2280 ms, 99th 1177 ms.
  - `api/v1/projects` (GET PROJECTS): Max 2818 ms, 95th 1004 ms.
  - `api/v1/users` (GET ALL USERS): Max 2357 ms, 99th 715 ms.
  - After optimisations, all percentiles and maxima are below 2000 ms.
- **Improvement**: Average reductions (e.g., `api/v1/projects` (GET PROJECTS): 534 ms → 48 ms) are mirrored by percentile drops (e.g., 95th from 1004 ms to 72 ms).

---

## Throughput Analysis

### Throughput Metrics

Throughput is measured as requests per second (req/s) from both reports.

| Endpoint | Throughput (req/s) Before (Agg/Sum) | Throughput (req/s) After (Agg/Sum) |
| --- | --- | --- |
| api/v1/tasks (GET ALL TASKS) | 6.63130/6.63130 | 6.67913/6.67913 |
| api/v1/tasks (CREATE TASK REQUEST) | 6.65613/6.65613 | 6.72518/6.72518 |
| api/v1/projects (GET PROJECTS) | 6.65480/6.65480 | 6.76373/6.76373 |
| api/v1/users (GET ALL USERS) | 6.70781/6.70781 | 6.78771/6.78771 |
| api/v1/users/{id}/tasks (GET USER TASKS) | 6.71580/6.71580 | 6.79740/6.79740 |
| TOTAL | 32.95056/32.95056 | 33.32500/33.32500 |

### Throughput Comparison (Aggregate vs. Summary)

- **Perfect Alignment**: Throughput values are identical between aggregate and summary reports, confirming consistency across the 200-thread, loop count of 2 configuration.
- **Added Insight**: The aggregate data reinforces the 1.1% overall throughput increase (32.95 → 33.33 req/s), with no variation.

### Throughput Observations

- **Slight Increase**: Throughput improved slightly across all endpoints (e.g., `api/v1/projects` (GET PROJECTS): 6.65 → 6.76 req/s).
- **No Drops**: Stability under the 60-second ramp-up indicates robust optimisation.

---

## Error Rate Analysis

### Error Rate Metrics

Error rates are derived from both reports.

| Endpoint | Error % Before (Agg/Sum) | Error % After (Agg/Sum) |
| --- | --- | --- |
| api/v1/tasks (GET ALL TASKS) | 0.000%/0.000% | 0.000%/0.000% |
| api/v1/tasks (CREATE TASK REQUEST) | 0.000%/0.000% | 0.000%/0.000% |
| api/v1/projects (GET PROJECTS) | 0.000%/0.000% | 0.000%/0.000% |
| api/v1/users (GET ALL USERS) | 0.000%/0.000% | 0.000%/0.000% |
| api/v1/users/{id}/tasks (GET USER TASKS) | 0.000%/0.000% | 0.000%/0.000% |
| TOTAL | 0.000%/0.000% | 0.000%/0.000% |

### Error Rate Comparison (Aggregate vs. Summary)

- **Consistency**: Both reports show 0.000% error rates, indicating no discrepancies.
- **Added Insight**: The aggregate data confirms no non-200 status codes across all 2000 samples.

### Error Rate Observations

- **No Errors**: Suggests the 200-thread test may not have stressed the system sufficiently.

---

## Latency Percentiles Analysis

### Latency Percentile Metrics

Percentile data is from aggregate reports. An SLA of 1000 ms for the 95th percentile is assumed.

- **Before Optimisations**:
  - `api/v1/tasks` (GET ALL TASKS): 95th 388 ms (SLA met).
  - `api/v1/projects` (GET PROJECTS): 95th 1004 ms (SLA met, at threshold).
  - `api/v1/users` (GET ALL USERS): 95th 205 ms (SLA met).
  - `api/v1/users/{id}/tasks` (GET USER TASKS): 95th 277 ms (SLA met).
- **After Optimisations**:
  - `api/v1/tasks` (GET ALL TASKS): 95th 133 ms (SLA met).
  - `api/v1/projects` (GET PROJECTS): 95th 72 ms (SLA met).
  - `api/v1/users` (GET ALL USERS): 95th 82 ms (SLA met).
  - `api/v1/users/{id}/tasks` (GET USER TASKS): 95th 94 ms (SLA met).

### Latency Percentile Comparison (Aggregate vs. Summary)

- **Aggregate Advantage**: Percentiles (e.g., 95th, 99th) are only in aggregate, providing tail latency data (e.g., `api/v1/projects` (GET PROJECTS) 99th from 1322 ms to 281 ms) absent in summary.
- **Summary Limitation**: Summary lacks percentile insight, relying on max (e.g., 2818 ms).

### Latency Percentile Observations

- **Pre-Optimisation**: `api/v1/projects` (GET PROJECTS)’ 95th (1004 ms) was at the SLA edge.
- **Post-Optimisation**: All 95th percentiles are below 133 ms.

---

## Additional Observations

- **Data Volume**: Received KB/sec dropped (e.g., `api/v1/projects` (GET PROJECTS): 46.77 → 17.95), consistent across reports.
- **Load Context**: The 200-thread, 60-second ramp-up and loop count of 2 provide a moderate load.

---

## Conclusion

Optimisations reduced response times (e.g., `api/v1/projects` (GET PROJECTS) from 534 ms to 48 ms) and 95th percentiles (e.g., 1004 ms to 72 ms), with throughput increasing slightly (32.95 → 33.33 req/s). 
Aggregate data enhances analysis with percentiles, revealing tail latency improvements. No errors suggest further stress testing is needed.

### Action Plan

1. **Validate Optimisations**: Test with 300 threads.
2. **Enhance Monitoring**: Add percentiles and timestamps.
3. **Stress Test**: Simulate failures.
4. **Optimise Payloads**: Ensure reduced KB/sec is sustainable.
5. **Review SLA**: Adjust to 500 ms for 95th percentile.
