# Stage 0: Download dependencies (cached unless build.gradle changes)
FROM eclipse-temurin:27-jdk AS deps
WORKDIR /app
COPY build.gradle settings.gradle gradlew gradle.properties ./
COPY gradle/wrapper gradle/wrapper
COPY dora-neuron/build.gradle dora-neuron/build.gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --no-configuration-cache

# Stage 1: Build Java application (leverages dependency cache from Stage 0)
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY --from=deps /root/.gradle/caches /root/.gradle/caches
COPY build.gradle settings.gradle gradlew gradle.properties ./
COPY gradle/wrapper gradle/wrapper
COPY src src
COPY dora-neuron dora-neuron
# Rule files are needed by processResources to include them in the JAR
COPY starship-troopers-rules starship-troopers-rules
COPY star-wars-rules star-wars-rules
COPY openttd-rules openttd-rules
COPY star-trek-rules star-trek-rules
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon --no-configuration-cache

# Stage 1b: Generate web client config (runs after Java build)
FROM eclipse-temurin:27-jdk AS web-config
WORKDIR /app
COPY build.gradle settings.gradle gradlew gradle.properties ./
COPY gradle/wrapper gradle/wrapper
COPY dora-web dora-web
ARG WEB_ENV=prod
ARG WEB_API_URL=
RUN chmod +x gradlew && \
    if [ -n "$WEB_API_URL" ]; then \
        ./gradlew generateWebConfig --no-daemon --no-configuration-cache -Dweb.env=$WEB_ENV -Dweb.api.url=$WEB_API_URL; \
    else \
        ./gradlew generateWebConfig --no-daemon --no-configuration-cache -Dweb.env=$WEB_ENV; \
    fi

# Stage 2: Runtime base (shared by runtime and prod targets)
FROM eclipse-temurin:27-jre AS runtime
WORKDIR /app

# Swap is created at runtime only when DL_MODE!=rest to avoid baking an 8GB file into the image.
# The ENTRYPOINT will create and activate swap on first start if needed.

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

# JVM memory defaults match gradle.properties: app.jvm.args + app.javacpp.args.
# Override at runtime: docker run -e JVM_XMX="-Xmx8G" ...
# When DL_MODE=rest, DL4J is not loaded — lower memory is sufficient.
# When DL_MODE=local, DL4J memory args are needed for neural network training.
ENV JVM_ARGS="-Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV JAVACPP_ARGS="-Dorg.bytedeco.javacpp.maxbytes=2G -Dorg.bytedeco.javacpp.maxphysicalbytes=2G -Dorg.bytedeco.javacpp.alloctype=direct"

ENTRYPOINT ["sh", "-c", "\
  # Create and activate swap at runtime only when DL_MODE!=rest (avoids 8GB image bloat) \
  if [ \"$DL_MODE\" != \"rest\" ] && [ ! -f /swapfile ]; then \
    SWAP_SIZE=${SWAP_SIZE:-8G} && \
    SWAP_MB=$(( ${SWAP_SIZE%G} * 1024 )) && \
    dd if=/dev/zero of=/swapfile bs=1M count=$SWAP_MB 2>/dev/null && \
    chmod 600 /swapfile && \
    mkswap /swapfile && \
    swapon /swapfile 2>/dev/null && echo \"Swap activated (${SWAP_SIZE})\" || echo \"Swap activation skipped (container not privileged)\"; \
  elif [ \"$DL_MODE\" != \"rest\" ] && [ -f /swapfile ]; then \
    swapon /swapfile 2>/dev/null && echo 'Swap activated from existing file' || echo 'Swap activation skipped'; \
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
  java ${JVM_XMX:-$JVM_ARGS} $JAVACPP_ARGS \
    $DL_ARGS \
    $DEBUG_ARGS \
    -jar app.jar"]

# Production target — lower memory defaults, DL_MODE=rest by default
FROM runtime AS prod
ENV DL_MODE=rest
ENV JVM_ARGS="-Xmx1G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV JAVACPP_ARGS="-Dorg.bytedeco.javacpp.maxbytes=512M -Dorg.bytedeco.javacpp.maxphysicalbytes=512M -Dorg.bytedeco.javacpp.alloctype=direct"
