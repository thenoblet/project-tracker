# Garbage Collector Report (JProfiler and Prometheus/Grafana)

App was run with
``bash
java -Xmx512m -Xms512m -XX:+UseG1GC -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError -jar target/project-tracker-0.0.1-SNAPSHOT.jar
``

| Flag                              | Meaning                                                                  |
| --------------------------------- | ------------------------------------------------------------------------ |
| `-Xms512m -Xmx512m`               | Sets both initial and max heap size to 512 MB. Avoids resizing overhead. |
| `-XX:+UseG1GC`                    | Uses G1 garbage collector for predictable short pauses.                  |
| `-XX:+PrintGCDetails`             | Prints detailed GC logs to stdout for additional analysis.               |
| `-XX:+HeapDumpOnOutOfMemoryError` | Dumps heap to file if an OOM occurs, for offline analysis.               |


#### Load Testing Setup (JMeter)
| Parameter       | Value                                                 |
| --------------- | ----------------------------------------------------- |
| Number of Users | **200** concurrent threads                            |
| Ramp-up Period  | **60 seconds** (gradual load increase)                |
| Loop Count      | **Infinite**                                          |
| Duration        | Test manually stopped after desired observation window |


### ♻️ Garbage Collection Behavior (JProfiler)

#### Observations
- Under load, the GC was active with short bursts.
- Most GC activity stayed below **10% per second**, with occasional peaks reaching **15%**.
- This indicates the garbage collector spent ~50-150 ms per second doing collections.

#### Interpretation
- These are acceptable pause times for a typical web application.
- No excessive full GCs observed; the heap was being efficiently managed by the GC.

#### Screenshot
![GC activity chart](images/gc-activity.png)

#### Notes
- Data captured using **JProfiler VM Telemetry → Garbage Collector**.
- Load applied using **JMeter** simulating 200 concurrent users.

### ♻️ Garbage Collection Metrics (Grafana / Prometheus)

#### Observations
Captured using Grafana dashboards scraping `/actuator/prometheus` metrics from the Spring Boot app.

| Metric                  | Observed Values                  |
|---------------------------|--------------------------------|
| GC Frequency              | Peaked at ~0.15 ops/s (~1 GC every 6 sec) |
| Max GC Pause              | ~500 ms during early spikes    |
| Typical GC Pause          | ~100 ms after stabilizing      |
| Allocation Rate           | Increased up to ~50 MB/s under load |
| Promotion Rate            | Mostly low, indicating short-lived objects |

#### Interpretation
- GC was responsive, quickly reclaiming memory under load.
- Most pauses stayed around **100 ms**, acceptable for web latency targets.
- Initial ~500 ms spikes may indicate warm-up or large allocations during test ramp-up.

#### Screenshot
![GC metrics](images/grafana-gc.png)

#### Notes
- Data pulled from Spring Boot actuator metrics exposed to Prometheus.
- Visualized in Grafana using JVM GC dashboards.
