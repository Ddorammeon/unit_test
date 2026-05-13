# ========================================================
# STAGE 1: Build Stage
# ========================================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace

# 1. Copy file cấu hình Gradle để tận dụng Cache
# Chỉ copy build.gradle của common
COPY service/common/build.gradle /workspace/common/
# Copy đầy đủ cấu hình của insurance-service
COPY service/insurance-service/build.gradle service/insurance-service/settings.gradle /workspace/insurance-service/

# 2. Build Module Common (Bắt buộc vì insurance-service cần thư viện này)
COPY service/common/src /workspace/common/src
WORKDIR /workspace/common
# Publish common vào mavenLocal bên trong container
RUN gradle publishToMavenLocal -x test --no-daemon

# 3. Build Insurance Service
COPY service/insurance-service/src /workspace/insurance-service/src
WORKDIR /workspace/insurance-service
# Build ra file .jar (bỏ qua test để tăng tốc)
RUN gradle bootJar -x test --no-daemon

# ========================================================
# STAGE 2: Runtime Stage
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copy file JAR từ giai đoạn builder sang
COPY --from=builder /workspace/insurance-service/build/libs/*.jar app.jar

# 2. Cấu hình RAM tự động (Giới hạn 75% RAM container)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 3. Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]