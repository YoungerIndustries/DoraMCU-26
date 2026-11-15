# DoraMCU Debugging Guide

This guide covers how to debug the DoraMCU application locally, in Docker containers, and in Kubernetes clusters. It also covers DL4J memory debugging and JVM crash troubleshooting.

---

## Table of Contents

1. [Local IDE Debugging](#1-local-ide-debugging)
2. [Remote Debugging from Docker](#2-remote-debugging-from-docker)
3. [Remote Debugging from Kubernetes](#3-remote-debugging-from-kubernetes)
4. [DL4J Memory Debugging](#4-dl4j-memory-debugging)
5. [JVM Crash Troubleshooting](#5-jvm-crash-troubleshooting)
6. [Debugger Best Practices](#6-debugger-best-practices)
7. [Common Debugging Scenarios](#7-common-debugging-scenarios)

---

## 1. Local IDE Debugging

### Run Configurations

Two shared run configurations are provided in `.idea/runConfigurations/`:

- **`DoraMcuLauncher-debug.xml`** — Debug the main application via Gradle `bootRun`. Uses JDWP on port **5005**. Inherits all JVM memory settings from `build.gradle` (4G heap, G1GC, JavaCPP settings).
- **`Gradle-Test-debug.xml`** — Debug tests via Gradle `test`. Uses JDWP on port **5006** to avoid port conflicts with the application debug.

### How to Use

1. Open the project in IntelliJ IDEA.
2. In the Run/Debug Configurations dropdown, select **`DoraMcuLauncher-debug`** or **`Gradle-Test-debug`**.
3. Set breakpoints in your source code.
4. Click the Debug button (or press `Shift+F9`).

### JVM Memory Settings

The `build.gradle` configures the following JVM arguments for `bootRun`:

```
-Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200
-Dorg.bytedeco.javacpp.maxbytes=2G
-Dorg.bytedeco.javacpp.maxphysicalbytes=2G
-Dorg.bytedeco.javacpp.alloctype=direct
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

The `test` task uses the same settings (without JDWP). The debug configurations preserve these settings.

### Active Spring Profiles

The application defaults to the `dev` profile when run locally. You can override this by adding `-Dspring.profiles.active=mysql` to the VM options if you need MySQL instead of H2.

---

## 2. Remote Debugging from Docker

### Enabling Debug Mode

The Docker image supports optional remote debugging via the `DEBUG` environment variable:

```bash
# Start with debugging enabled
DEBUG=true docker-compose up -d app
```

When `DEBUG=true`, the container starts with JDWP on port **5005**.

### Port Mapping

The `docker-compose.yml` maps the debug port:

```yaml
ports:
  - "8080:8080"
  - "${DEBUG_PORT:-5005}:5005"
```

The host port can be customized via the `DEBUG_PORT` environment variable.

### Connecting from IntelliJ IDEA

1. Start the container with debugging enabled:
   ```bash
   DEBUG=true docker-compose up -d app
   ```
2. In IntelliJ IDEA, create a **Remote JVM Debug** configuration:
   - Host: `localhost`
   - Port: `5005`
   - Use module classpath from the current project
3. Start the debug configuration. IDEA will attach to the running container.
4. Set breakpoints and trigger the code paths you want to investigate.

### Disabling Debug Mode

To run without debugging (default):

```bash
docker-compose up -d app
```

or explicitly:

```bash
DEBUG="" docker-compose up -d app
```

---

## 3. Remote Debugging from Kubernetes

### Dev Overlay

The `k8s/overlays/dev/` overlay includes a debug patch (`debug-patch.yaml`) that:

- Adds port **5005** to the app container
- Sets `DEBUG=true` environment variable
- Overrides the container command to include JDWP arguments
- Exposes the debug port in the Service

### Deploying with Debugging

```bash
# Apply the dev overlay (includes debug patch)
kubectl apply -k k8s/overlays/dev/
```

### Port Forwarding

```bash
# Forward local port 5005 to the pod's debug port
kubectl port-forward deployment/doramcu-app 5005:5005 -n doramcu
```

### Connecting from IntelliJ IDEA

1. Apply the dev overlay and start port forwarding as shown above.
2. In IntelliJ IDEA, create a **Remote JVM Debug** configuration:
   - Host: `localhost`
   - Port: `5005`
3. Start the debug configuration.
4. Set breakpoints and investigate.

### Important Notes

- **Do not use the debug patch in production.** It is only in the `dev` overlay.
- Remote debugging in Kubernetes adds latency. Use logpoints (see below) for high-frequency code paths.
- The debug patch overrides the container's command and args. Ensure the JAR path (`/app/app.jar`) matches the Dockerfile's WORKDIR.

---

## 4. DL4J Memory Debugging

The DoraMCU project has experienced DL4J OutOfMemoryError crashes and JVM native memory allocation failures. The crash dumps (`dl4j-memory-crash-dump-*.txt`, `hs_err_pid12052.log`) document these issues.

### Understanding DL4J Memory

DL4J uses both JVM heap and native (off-heap) memory via JavaCPP:

| Setting | Value | Description |
|---------|-------|-------------|
| `-Xmx4G` | 4 GB | JVM heap maximum |
| `-Dorg.bytedeco.javacpp.maxbytes` | 2G | Native memory limit for JavaCPP |
| `-Dorg.bytedeco.javacpp.maxphysicalbytes` | 2G | Physical memory limit (includes GPU) |

### Analyzing Crash Dumps

The project contains crash dump files:

- **`dl4j-memory-crash-dump-*.txt`** — DL4J-specific memory crash logs
- **`hs_err_pid*.log`** — JVM crash logs (HotSpot error logs)

To analyze a JVM crash log:

1. Open `hs_err_pid*.log` in a text editor.
2. Look for the **Native frames** section to identify the native library where the crash occurred.
3. Check the **Java threads** section for the thread that was executing when the crash happened.
4. Look for **Memory** information to see heap and native memory usage at crash time.

### JavaCPP Memory Monitoring

To monitor JavaCPP memory usage at runtime, add a logpoint in your code:

```java
// Logpoint expression (non-suspending)
"JavaCPP memory: " + org.bytedeco.javacpp.Pointer.nativeDeallocator()
```

Or use the built-in JavaCPP logging:

```bash
# Enable JavaCPP debug logging
java -Dorg.bytedeco.javacpp.debug=true ...
```

### Reducing DL4J Memory Pressure

If you encounter DL4J memory issues:

1. **Use REST mode** — Set `DL_MODE=rest` to offload DL4J training to the Python microservice (`dora-dl-python`). This eliminates the need for large JVM memory for neural network training.
2. **Reduce batch size** — If training in-process, reduce the mini-batch size in your DL4J configuration.
3. **Increase swap** — The Dockerfile creates an 8GB swap file. You can increase it:
   ```bash
   docker build --build-arg SWAP_SIZE=16G -t doramcu/app .
   ```
4. **Use G1GC** — The application uses G1GC (`-XX:+UseG1GC`) which is better for large heap applications.

---

## 5. JVM Crash Troubleshooting

### hs_err_pid Log Analysis

When the JVM crashes, it generates an `hs_err_pid<process_id>.log` file in the working directory.

Key sections to examine:

1. **Problematic frame** — Shows the native method where the crash occurred.
2. **Thread** — The Java thread that was executing.
3. **Registers** — CPU register state at crash time.
4. **Memory** — Heap and metaspace usage.
5. **Library list** — Loaded native libraries.

### Generating Thread Dumps

To generate a thread dump without crashing the application:

```bash
# Using jcmd (recommended)
jcmd <pid> Thread.print

# Using jstack
jstack <pid> > thread_dump.txt

# Using kill signal (Unix)
kill -3 <pid>
```

Thread dumps are useful for:

- Identifying deadlocks
- Finding threads blocked on I/O
- Detecting GC pressure
- Understanding thread state during memory issues

### JVM Flags for Diagnostics

Add these flags to enable more diagnostic information:

```bash
# Enable GC logging
-XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:LogFile=gc.log

# Enable native memory tracking
-XX:NativeMemoryTracking=summary

# Print GC details
-Xlog:gc*:file=gc.log:time,uptime,level,tags
```

---

## 6. Debugger Best Practices

### Logpoint-First Approach

**Use logpoints as your primary debugging tool.** Logpoints are non-suspecting breakpoints that print messages without pausing execution.

Benefits:
- No performance impact from suspending threads
- Can trace execution flow across multiple calls
- Safe for concurrent code
- No risk of deadlocks from debugger suspension

How to use logpoints in IntelliJ IDEA:
1. Right-click the gutter next to a line → **Add Logpoint**
2. Enter a message template, e.g., `Processing entity: {entity.getId()}`
3. Run the application normally (not in debug mode)
4. Logpoint messages appear in the Run/Debug console

### Breakpoint Strategy

1. **Start with logpoints** to understand the execution flow.
2. **Add conditional breakpoints** to narrow down specific cases.
3. **Use regular breakpoints** only when you need to inspect state interactively.
4. **Use class load breakpoints** to investigate when/where a class is loaded.

### Remote Debugging Tips

- **Minimize breakpoints** in remote debugging — each suspension adds latency.
- **Use logpoints** for high-frequency code paths.
- **Port conflicts** — JDWP port 5005 may conflict with other services. Use 5006 for tests.
- **Memory constraints** — Debugging with full JVM args requires sufficient system memory.

---

## 7. Common Debugging Scenarios

### Scenario 1: DL4J Training OOM

**Symptoms**: Application crashes with `OutOfMemoryError` during neural network training.

**Steps**:
1. Check the crash dump files for the exact error.
2. Switch to REST mode: `DL_MODE=rest` in environment variables.
3. If in-process training is required, add logpoints before training to monitor memory:
   ```java
   // Logpoint before training
   "Heap used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB"
   ```
4. Reduce batch size or increase swap.

### Scenario 2: API Endpoint Not Responding

**Symptoms**: A specific API endpoint hangs or returns 500 errors.

**Steps**:
1. Add a logpoint at the controller method entry.
2. Add logpoints at key service layer methods.
3. Check if the request reaches the controller.
4. If it does, trace through the service layer to find where it hangs.
5. Use a regular breakpoint if you need to inspect request/response objects.

### Scenario 3: Database Connection Issues

**Symptoms**: Application fails to connect to MySQL or connection pool exhaustion.

**Steps**:
1. Add logpoints in the database configuration class.
2. Check connection pool settings in `application.properties`.
3. Use thread dumps to see if threads are blocked waiting for connections.
4. Verify the database container is healthy: `docker-compose ps`.

### Scenario 4: Neuron Network Behavior

**Symptoms**: Unexpected behavior in the neuron network (mesh, consensus, etc.).

**Steps**:
1. Add logpoints in neuron-related service classes.
2. Use the debugger to step through consensus or mesh logic.
3. Check logs for neuron-related warnings or errors.
4. In Kubernetes, use `kubectl logs` to see pod logs alongside debugging.

### Scenario 5: JVM Native Memory Crash

**Symptoms**: JVM crashes with a native error (segfault, allocation failure).

**Steps**:
1. Locate the `hs_err_pid*.log` file.
2. Check the problematic frame for the native library involved.
3. Look at the thread state to understand what the application was doing.
4. Check if JavaCPP native memory limits are being exceeded.
5. Consider enabling native memory tracking: `-XX:NativeMemoryTracking=summary`.

---

## Quick Reference

| Environment | Debug Port | How to Enable |
|-------------|------------|---------------|
| Local (IDEA) | 5005 | Run `DoraMcuLauncher-debug` config |
| Local Tests | 5006 | Run `Gradle-Test-debug` config |
| Docker | 5005 | `DEBUG=true docker-compose up -d app` |
| Kubernetes (dev) | 5005 | `kubectl apply -k k8s/overlays/dev/` + port-forward |

| JVM Setting | Value | Purpose |
|-------------|-------|---------|
| `-Xmx4G` | 4 GB | Heap maximum |
| `-XX:+UseG1GC` | — | Garbage collector |
| `-Dorg.bytedeco.javacpp.maxbytes` | 2G | Native memory limit |
| `-Dorg.bytedeco.javacpp.maxphysicalbytes` | 6G | Physical memory limit |
