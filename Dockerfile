# Stage 1: Build Java application
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle/wrapper gradle/wrapper
COPY src src
COPY dora-neuron dora-neuron
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Stage 1b: Generate web client config (runs after Java build)
FROM eclipse-temurin:27-jdk AS web-config
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle/wrapper gradle/wrapper
COPY dora-web dora-web
ARG WEB_ENV=prod
ARG WEB_API_URL=
RUN chmod +x gradlew && \
    if [ -n "$WEB_API_URL" ]; then \
        ./gradlew generateWebConfig --no-daemon -Dweb.env=$WEB_ENV -Dweb.api.url=$WEB_API_URL; \
    else \
        ./gradlew generateWebConfig --no-daemon -Dweb.env=$WEB_ENV; \
    fi

# Stage 2: Run Java application
FROM eclipse-temurin:27-jre
WORKDIR /app

# Create swap file on disk for local DL4J mode (must be done as root).
# When DL_MODE=rest, swap is not needed (Python service handles ML separately).
# Override at build time: docker build --build-arg SWAP_SIZE=8G ...
ARG SWAP_SIZE=8G

# Create the swap file at build time using the ARG value
RUN dd if=/dev/zero of=/swapfile bs=1M count=$(( ${SWAP_SIZE%G} * 1024 )) \
    && chmod 600 /swapfile \
    && mkswap /swapfile

# Run as non-root user for security
RUN groupadd -r doramcu && useradd -r -g doramcu -d /app -s /sbin/nologin doramcu
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R doramcu:doramcu /app
USER doramcu

EXPOSE 8080
EXPOSE 5005

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# DL_MODE: Set to "rest" to use the Python DL microservice (dora-dl-python) instead of local DL4J.
# When using REST mode, the Java app calls the Python service on port 8083 via HTTP.
# This eliminates the need for large JVM memory and swap for DL4J training.
# Default: "local" (uses DL4J in-process)
ENV DL_MODE=local

# DEBUG: Set to "true" to enable remote debugging via JDWP on port 5005.
# Use with caution: debugging adds overhead and should not be used in production.
# Default: "" (disabled)
ENV DEBUG=

# JVM memory standardized to match gradle.properties (app.jvm.args, app.javacpp.args).
# Override at runtime: docker run -e JVM_XMX="-Xmx8G" ...
# When DL_MODE=rest, DL4J is not loaded — lower memory is sufficient.
# When DL_MODE=local, DL4J memory args are needed for neural network training.
# JVM defaults match gradle.properties: app.jvm.args + app.javacpp.args
ENV JVM_ARGS="-Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV JAVACPP_ARGS="-Dorg.bytedeco.javacpp.maxbytes=2G -Dorg.bytedeco.javacpp.maxphysicalbytes=2G -Dorg.bytedeco.javacpp.alloctype=direct"
ENTRYPOINT ["sh", "-c", "\
  # Activate swap file if not already active (only needed for local DL4J mode; \
  # requires privileged container — silently skip if permission denied) \
  if [ \"$DL_MODE\" != \"rest\" ] && ! swapon --show 2>/dev/null | grep -q /swapfile; then \
    swapon /swapfile 2>/dev/null && echo 'Swap activated on disk' || echo 'Swap activation skipped (container not privileged)'; \
  fi && \
  # Set DL REST client property based on DL_MODE \
  DL_ARGS=\"\" && \
  if [ \"$DL_MODE\" = \"rest\" ]; then \
    DL_ARGS=\"-Ddl.rest-client.enabled=true\"; \
  fi && \
  # Set debug arguments based on DEBUG env var \
  DEBUG_ARGS=\"\" && \
  if [ \"$DEBUG\" = \"true\" ]; then \
    DEBUG_ARGS=\"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\" && \
    echo 'Debug mode enabled on port 5005'; \
  fi && \
  java ${JVM_XMX:--Xmx4G} -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
    -Dorg.bytedeco.javacpp.maxbytes=2G \
    -Dorg.bytedeco.javacpp.maxphysicalbytes=2G \
    -Dorg.bytedeco.javacpp.alloctype=direct \
    $DL_ARGS \
    $DEBUG_ARGS \
    -jar app.jar"]
