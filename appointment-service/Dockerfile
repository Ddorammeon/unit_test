# ========================================================
# STAGE 1: Build Stage
# ========================================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace

# 1. Copy file cấu hình Gradle để tận dụng Cache
# Copy build.gradle của common (Lưu ý: chỉ copy build.gradle để tránh lỗi nếu thiếu settings.gradle)
COPY service/common/build.gradle /workspace/common/
# Copy build.gradle và settings.gradle của appointment-service
COPY service/appointment-service/build.gradle service/appointment-service/settings.gradle /workspace/appointment-service/

# 2. Build Module Common (Bắt buộc vì appointment-service phụ thuộc vào nó)
COPY service/common/src /workspace/common/src
WORKDIR /workspace/common
# Publish common vào mavenLocal của container
RUN gradle publishToMavenLocal -x test --no-daemon

# 3. Build Appointment Service
COPY service/appointment-service/src /workspace/appointment-service/src
WORKDIR /workspace/appointment-service
# Build ra file .jar (bỏ qua test)
RUN gradle bootJar -x test --no-daemon

# ========================================================
# STAGE 2: Runtime Stage
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copy file JAR từ builder sang
COPY --from=builder /workspace/appointment-service/build/libs/*.jar app.jar

# 2. Cấu hình RAM tự động (75% RAM container)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 3. Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]