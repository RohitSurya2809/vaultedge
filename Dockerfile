# ---------- build stage ----------
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /workspace

# copy maven config first for caching
COPY pom.xml ./
# prefetch dependencies (improves caching)
RUN mvn -B -ntp -DskipTests dependency:go-offline

# copy source
COPY src src

# package (skip tests for faster build; remove -DskipTests if you want tests in image build)
RUN mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-alpine

# create non-root user
RUN addgroup -S app && adduser -S -G app app
USER app

WORKDIR /app

# copy jar from build stage
COPY --from=build /workspace/target/*.jar app.jar

# default JVM options (can be overridden)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Expose port
EXPOSE 8080

# Healthcheck uses actuator; ensure you have spring-boot-starter-actuator on classpath
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- --timeout=2 http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
