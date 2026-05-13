# ========================================================
# STAGE 1: Build Stage
# ========================================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace

# 1. Copy file cấu hình Gradle để tận dụng Cache Layer
# Chỉ copy build.gradle của common
COPY service/common/build.gradle /workspace/common/
# Copy đầy đủ cấu hình của prescription-service
COPY service/prescription-service/build.gradle service/prescription-service/settings.gradle /workspace/prescription-service/

# 2. Build Module Common (Bắt buộc vì prescription-service cần thư viện này)
COPY service/common/src /workspace/common/src
WORKDIR /workspace/common
# Publish common vào mavenLocal bên trong container
RUN gradle publishToMavenLocal -x test --no-daemon

# 3. Build Prescription Service
COPY service/prescription-service/src /workspace/prescription-service/src
WORKDIR /workspace/prescription-service
# Build ra file .jar (bỏ qua test để tăng tốc độ build)
RUN gradle bootJar -x test --no-daemon

# ========================================================
# STAGE 2: Runtime Stage
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copy file JAR từ giai đoạn builder sang
COPY --from=builder /workspace/prescription-service/build/libs/*.jar app.jar

# 2. Cấu hình RAM tự động (Giới hạn 75% RAM container)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 3. Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]