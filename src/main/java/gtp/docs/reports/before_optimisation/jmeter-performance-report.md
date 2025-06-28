# JMeter Performance Test Analysis Report

## Overview

This report analyses the performance of a web application based on JMeter test results provided in the `Jmeter`. 
The analysis covers response times, throughput, error rates, and latency percentiles for six endpoints: 
- `api/v1/tasks` - GET ALL TASKS 
- `api/v1/tasks` - CREATE TASK REQUEST 
- `api/v1/projects` - GET PROJECTS 
- `api/v1/projects` - CREATE PROJECT 
- `api/v1/auth/login` - Login 
- `api/v1/users` - GET ALL USERS 

The goal is to identify performance bottlenecks, anomalies, and provide actionable recommendations to meet service-level agreements (SLAs).

---
# 1.  Before Performance Tuning

## Response Time Analysis

### Response Time Metrics

The table below summarises the minimum, average, and maximum response times (in milliseconds) for each endpoint, as derived from the `summary.csv` file.

| Endpoint | Min (ms) | Avg (ms) | Max (ms) | Std. Dev. (ms) |
| --- | --- | --- | --- | --- |
| GET ALL TASKS REQUEST | 27 | 38 | 123 | 11.20 |
| CREATE TASK REQUEST | 23 | 35 | 123 | 10.56 |
| GET PROJECTS | 150 | 240 | 530 | 53.30 |
| CREATE PROJECT | 23 | 36 | 152 | 11.85 |
| Login | 159 | 205 | 359 | 25.98 |
| GET ALL USERS | 27 | 38 | 116 | 11.49 |
| TOTAL | 23 | 102 | 530 | 93.10 |

### Response Time Anomalies

- **Threshold Exceedance**: Assuming a response time SLA of 2 seconds (2000 ms), no endpoint exceeds this threshold for average response times. However:
  - **GET PROJECTS** has a maximum response time of **530 ms**, which is significantly higher than other endpoints (most max out around 116–359 ms).
  - **Login** has a high average response time of **205 ms** and a maximum of **359 ms**, indicating potential bottlenecks.
- **High Variability**: The **GET PROJECTS** endpoint shows a high standard deviation (53.30 ms), suggesting inconsistent performance, possibly due to database queries or resource contention.
- **Interesting Fact**: The **GET PROJECTS** endpoint's maximum response time (530 ms) is over 3.5x its minimum (150 ms), indicating potential spikes in latency that could degrade user experience during peak loads.

---

## Throughput Analysis

### Throughput Metrics

Throughput is measured as requests per second (req/s) and is derived from the `summary.csv` file.

| Endpoint | Throughput (req/s) | Samples |
| --- | --- | --- |
| GET ALL TASKS REQUEST | 0.26963 | 300 |
| CREATE TASK REQUEST | 0.26963 | 300 |
| GET PROJECTS | 0.26959 | 300 |
| CREATE PROJECT | 0.26963 | 300 |
| Login | 0.26959 | 300 |
| GET ALL USERS | 4.21026 | 200 |
| TOTAL | 1.52727 | 1700 |

### Throughput Observations

- **GET ALL USERS Outlier**: The GET ALL USERS endpoint has a significantly higher throughput (4.21 req/s) compared to other endpoints (\~0.27 req/s). This could be due to:
  - Fewer samples (200 vs. 300 for others), which may skew the throughput calculation.
  - Potentially lighter processing requirements or caching.
- **Low Throughput Across Most Endpoints**: Most endpoints have a throughput of \~0.27 req/s, which is unusually low for a performance test. This suggests either a low load test configuration or a bottleneck in the test environment (e.g., JMeter configuration, server capacity, or network latency).
- **Interesting Fact**: The GET ALL USERS endpoint’s throughput is \~15x higher than others, yet it processes fewer samples (200 vs. 300). This discrepancy suggests test configuration inconsistencies or endpoint-specific optimization.

### Recommendations

- **Validate Test Configuration**: Investigate why throughput is consistently low (\~0.27 req/s) for most endpoints. Check JMeter settings (e.g., thread count, ramp-up period) and server capacity (e.g., connection limits).
- **Analyze GET ALL USERS**: Determine why GET ALL USERS has significantly higher throughput. If caching is involved, consider applying similar optimizations to other endpoints.
- **Increase Load Testing**: Gradually increase the load (e.g., more threads or virtual users) to stress-test the system and identify throughput limits.

---

## Error Rate Analysis

### Error Rate Metrics

The error rate is derived from the `summary.csv` file, representing the percentage of requests that resulted in non-200 status codes.

| Endpoint | Error % |
| --- | --- |
| GET ALL TASKS REQUEST | 0.000% |
| CREATE TASK REQUEST | 0.000% |
| GET PROJECTS | 0.000% |
| CREATE PROJECT | 0.000% |
| Login | 0.000% |
| GET ALL USERS | 0.000% |
| TOTAL | 0.000% |

### Error Rate Observations

- **No Errors Reported**: All endpoints report a 0.000% error rate, indicating no non-200 status codes (e.g., 500, 404) were encountered during the test.
- **Potential Concern**: While a 0% error rate is ideal, it may indicate:
  - A low-intensity test that didn’t push the system to its limits.
  - Overly permissive success criteria in JMeter (e.g., accepting 3xx or 4xx codes as "successful").
- **Interesting Fact**: The absence of errors across 1700 total samples is unusual for a performance test, especially with high max response times (e.g., 530 ms for GET PROJECTS). This suggests the test may not have stressed error-prone scenarios.

---

## Latency Percentile Analysis

### Latency Percentile Metrics

The table below summarises the 90th, 95th, and 99th percentile response times (in milliseconds) from the `aggregate.csv` file, compared against an assumed SLA of 1000 ms (1 second) for the 95th percentile.

| Endpoint | 90th %ile (ms) | 95th %ile (ms) | 99th %ile (ms) | SLA Violation (95th %ile &gt; 1000 ms) |
| --- | --- | --- | --- | --- |
| GET ALL TASKS REQUEST | 48 | 55 | 87 | No |
| CREATE TASK REQUEST | 42 | 47 | 88 | No |
| GET PROJECTS | 320 | 339 | 385 | No |
| CREATE PROJECT | 45 | 49 | 86 | No |
| Login | 234 | 244 | 288 | No |
| GET ALL USERS | 46 | 50 | 97 | No |

### Latency Percentile Observations

- **No SLA Violations**: All endpoints meet the assumed 95th percentile SLA of 1000 ms, with the highest 95th percentile being **339 ms** (GET PROJECTS).
- **GET PROJECTS Latency**: The GET PROJECTS endpoint has significantly higher percentiles (90th: 320 ms, 95th: 339 ms, 99th: 385 ms) compared to others, indicating that a small percentage of requests experience noticeable delays.
- **Login Latency**: The Login endpoint’s percentiles (90th: 234 ms, 95th: 244 ms, 99th: 288 ms) are also elevated, suggesting occasional slowdowns.
- **Interesting Fact**: The 99th percentile for GET PROJECTS (385 ms) is \~4.4x higher than the 99th percentile for GET ALL TASKS REQUEST (87 ms), highlighting a significant performance gap for tail-end requests.

---

## Conclusion

The JMeter test results indicate stable performance with no errors and response times well within the assumed 1000 ms SLA for the 95th percentile. However, key areas for improvement include:

1. **GET PROJECTS and Login Endpoints**: High maximum response times and variability suggest backend optimizations are needed (e.g., database indexing, caching).
2. **Low Throughput**: The \~0.27 req/s throughput for most endpoints is unusually low, indicating potential test configuration issues or server bottlenecks.
3. **Test Rigor**: The 0% error rate and low throughput suggest the test may not have stressed the system sufficiently. Increase load and include edge cases to uncover hidden issues.

### Action Plan

1. **Backend Optimization**: Prioritize GET PROJECTS and Login for query optimization and caching.
2. **Enhance Testing**: Increase load test intensity and ensure consistent sample sizes across endpoints.
3. **Monitoring**: Implement percentile-based monitoring and timestamped logging for trend analysis.
4. **Validate Configuration**: Review JMeter settings and server capacity to address low throughput.
