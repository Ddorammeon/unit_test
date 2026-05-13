# ========================================================
# STAGE 1: Build Stage
# ========================================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace

# 1. Copy file cấu hình Gradle để tận dụng Cache Layer
# Chỉ copy build.gradle của common (để tránh lỗi nếu thiếu settings.gradle)
COPY service/common/build.gradle /workspace/common/
# Copy đầy đủ cấu hình của clinical-service
COPY service/clinical-service/build.gradle service/clinical-service/settings.gradle /workspace/clinical-service/

# 2. Build Module Common (Bắt buộc vì clinical-service cần thư viện này cho Saga/Events)
COPY service/common/src /workspace/common/src
WORKDIR /workspace/common
# Publish common vào mavenLocal bên trong container
RUN gradle publishToMavenLocal -x test --no-daemon

# 3. Build Clinical Service
COPY service/clinical-service/src /workspace/clinical-service/src
WORKDIR /workspace/clinical-service
# Build ra file .jar (bỏ qua test để tăng tốc độ build)
RUN gradle bootJar -x test --no-daemon

# ========================================================
# STAGE 2: Runtime Stage
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copy file JAR từ giai đoạn builder sang
COPY --from=builder /workspace/clinical-service/build/libs/*.jar app.jar

# 2. Cấu hình RAM tự động (Giới hạn 75% RAM container để tránh OOM Kill)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 3. Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]