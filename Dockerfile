### ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy wrapper bits explicitly (avoid glob surprises)
COPY gradlew ./gradlew
COPY gradle/wrapper/ ./gradle/wrapper/

COPY settings.gradle* ./
COPY build.gradle* ./

# Make wrapper executable and normalize line endings
RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

# Only now copy sources (cache-friendly)
COPY src ./src

# Build (skip tests for speed; remove -x test if you want tests)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

# Stage the jar
RUN bash -lc 'ls build/libs/*.jar | head -n1 > /tmp/jar_path' \
 && install -D "$(cat /tmp/jar_path)" /app/app.jar


### ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
COPY --from=builder /app/app.jar /app/app.jar

RUN useradd -r -u 1001 spring && chown -R spring:spring /app
USER spring:spring

ENV SPRING_PROFILES_ACTIVE=local \
    SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/catalogdb \
    JAVA_OPTS=""

EXPOSE 8081
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
