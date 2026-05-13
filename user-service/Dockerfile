# ========================================================
# STAGE 1: Build Stage (Sử dụng Image Gradle chính hãng)
# ========================================================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace

# 1. Tối ưu Cache: Copy các file cấu hình Gradle trước
# Docker sẽ cache layer này nếu bạn không sửa đổi file build.gradle/settings.gradle
COPY service/common/build.gradle /workspace/common/
COPY service/user-service/build.gradle service/user-service/settings.gradle /workspace/user-service/

# 2. Build Module Common (Vì user-service phụ thuộc vào common)
# Copy source code của common
COPY service/common/src /workspace/common/src
WORKDIR /workspace/common
# Publish common vào mavenLocal của container để user-service có thể tìm thấy
RUN gradle publishToMavenLocal -x test --no-daemon

# 3. Build User Service
# Copy source code của user-service
COPY service/user-service/src /workspace/user-service/src
WORKDIR /workspace/user-service
# Build ra file .jar (bỏ qua test để build nhanh hơn)
RUN gradle bootJar -x test --no-daemon

# ========================================================
# STAGE 2: Runtime Stage (Chạy ứng dụng với JRE nhẹ)
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copy file JAR từ giai đoạn builder sang
# Lưu ý: Không cần tạo user spring/root, chạy mặc định với quyền root
COPY --from=builder /workspace/user-service/build/libs/*.jar app.jar

# 2. Cấu hình biến môi trường JVM cơ bản (Tự động nhận diện RAM container)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 3. Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]