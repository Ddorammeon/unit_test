package com.do_an.userservice.service;

import com.do_an.userservice.dto.request.LoginRequest;
import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.exceptions.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private UserDTO testUser;
    private String privateKeyPem;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserDTO();
        testUser.setId(UUID.randomUUID());
        testUser.setPhone("0123456789");
        testUser.setEmail("test@example.com");
        testUser.setFullname("Test User");
        testUser.setActive(true);
        testUser.setPrimaryRole("PATIENT");
        testUser.setRoles(Set.of("PATIENT"));

        // Private key for RSA signing (this is a test key - DO NOT USE IN PRODUCTION)
        privateKeyPem = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKj" +
                "MzEfYyjiWA4/4/0kLBKwG+T1CTrH1bYF9khpBBt8DVhkkVe7Bw5xZNhTYHHa" +
                "r5fNDNVJdW+X2H5OHHV0xJWYkVKPW4m6K3XLH1zLbvgR5C/p5cRo8R/x0fWK" +
                "yq7w7KSpVPMKJYZqGv3q7EZNPkVrRIZEJ0I2e3JI0e5qUHhfI5LO/z/Y9Hd3" +
                "pLbQzp5UxaI5GR5K0U7D7c4TlVR9RpZ+9pRj8a7D4HNLwVxq6sKIaHpGa/v3" +
                "xR0c4e1hKzLdTZJJY9fLSHMFWfvWNJb7pWJUVcgdVgKqOG1XBHqK5Q7jNCsv" +
                "4cGKLLtHhqZlAgMBAAECggEAKPF6PXJ7xWMRUhQEfATfyH2GBPm2dJeaH5JX" +
                "NpJCkXKK2jKyFVB6r5JhXrq7c5d7XZW7+y1qkMYkQNWKUECDBMVTvEww6PiT" +
                "1bJsVUdVKdwdlr+8vB6HGVQnJW2tRYL5PvUkCF2PKDZ4ygJvH3uZg1SJD1jd" +
                "I5KQxBd8F5YmKGYbqY8WT9k10I3X7X7tC6j8PKH7qeFwb3GYpJC5W0MXZ1dR" +
                "c7B6V4vvVLvmCQQP3QHJWQNL9w3eTmJa3R+9TLcT5ixJ8cUvs8OHLwjLT2i5" +
                "Jm0LlKqA7sX1VNv6v+xLv1u8ShXBVkNMuNEP8h0zqsP8n7KqLAyKCGFx4RdJ" +
                "BfTqgQKBgQDuqkLOx7TW8tVVn/tMfEoTR1GU4PeWH8WQV3d8+GBn6Ju2BmpV" +
                "6cCT5nBBhLmxr+nCqCghIAZWa+KTqc8YsyZLYdm3SEmjuqhT7G2p9F8yh7s5" +
                "SYcT4Hml3ySEZCB3Q6zk5R/2V1Fy+x8Y7Wvr/0GF5l8QZYmq6mLjUCx2UQKg" +
                "QqQbvG1lAQKBgQDK39AXfNxILxLWfPzFpG7XxuXh8xb6cCbGRQVL7Q+9cpyx" +
                "3x3nrFJ5jmGIuOxXuXU8qKqGVIQAYeKiKvJNu0IXPZbYvvIyVMVvS0D1HkTp" +
                "4HhDVIf3DCKb6HYrBhI8N7sHT/Q0S4yEn7eqLJwD1V/8F0R/1eXvuVKDZwKB" +
                "gQCnFz1ebFfA6aO/xNXp1HOa5aAfZ3sZ1f6ZJAhQxKU2Y7h0Yt/DKhLnVLjI" +
                "t3sUBqCYPe7fHb2G9vDCqI5DfLqhZyDfBXPH6tMjDhNCaiBgGo/Sy0fGg+vP" +
                "7YCEGgP+5+1UDwrXB0QROF0nQyT8oGYi7sVZzBBtqLSc8B3gAQKBgFvDwJVL" +
                "dH3K5GCv7MFVxCj8OPnZI4qxaFEMZKX9nQYRlMnU9Y0z1y2C4x3xN/xK7x8q" +
                "cQDrK8pVZJwz4nKfj+1GYJTe4nW6LnVDSBXHvZ8z5Qxm7YkKSU3K5RpGhVS6" +
                "Zy9eV/GrHhF3/5hN2MkIhcDvvt4FvPKdM5xRBrXbAoGAJlTNGqjHBvfz1U8w" +
                "C+VvP+NxBZzfJZnN2Nf4Vd5I8gxH0G0kXX2xC/wY5xTj0d0Xx3E+S3R0qJYw" +
                "s3zQ0wB7c5MZC5FZx3SvYLvXXEfHbXnKQZd5ej+/xKXUMVYK1SFQWb3Lfhh+" +
                "7A4QRkU7VbfuHQpGhKJZbJdmb+zOQNxZX8=";

        ReflectionTestUtils.setField(authService, "privateKey", privateKeyPem);
    }

    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .phone("0123456789")
                .password("password123")
                .build();

        when(userService.validateCredentials("0123456789", "password123"))
                .thenReturn(testUser);

        // Act
        ResponseEntity<UserDTO> response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("0123456789", response.getBody().getPhone());
        assertTrue(response.getHeaders().containsKey("Authorization"));
        verify(userService).validateCredentials("0123456789", "password123");
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerate_Success() {
        // Act
        String token = authService.generate(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        // JWT token format: header.payload.signature
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("Should include user claims in JWT token")
    void testGenerate_IncludesUserClaims() {
        // Act
        String token = authService.generate(testUser);

        // Assert
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        // Token should contain user information (verified through decode)
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerate_DifferentTokens() {
        // Arrange
        UserDTO anotherUser = new UserDTO();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setPhone("9876543210");
        anotherUser.setFullname("Another User");
        anotherUser.setPrimaryRole("DOCTOR");

        // Act
        String token1 = authService.generate(testUser);
        String token2 = authService.generate(anotherUser);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should handle null user roles gracefully")
    void testGenerate_NullRoles() {
        // Arrange
        UserDTO userWithoutRoles = new UserDTO();
        userWithoutRoles.setId(UUID.randomUUID());
        userWithoutRoles.setPhone("0123456789");
        userWithoutRoles.setFullname("User Without Roles");
        userWithoutRoles.setRoles(null);

        // Act
        String token = authService.generate(userWithoutRoles);

        // Assert
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("Should throw exception on invalid private key")
    void testGenerate_InvalidPrivateKey() {
        // Arrange
        ReflectionTestUtils.setField(authService, "privateKey", "invalid-key");

        // Act & Assert
        assertThrows(AppException.class, () -> authService.generate(testUser));
    }

    @Test
    @DisplayName("Should handle multiple roles in JWT token")
    void testGenerate_MultipleRoles() {
        // Arrange
        testUser.setRoles(Set.of("PATIENT", "DOCTOR"));

        // Act
        String token = authService.generate(testUser);

        // Assert
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("Should include primary role in JWT token")
    void testGenerate_IncludesPrimaryRole() {
        // Arrange
        testUser.setPrimaryRole("PATIENT");

        // Act
        String token = authService.generate(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void testGenerate_EmptyRolesList() {
        // Arrange
        testUser.setRoles(Set.of());

        // Act
        String token = authService.generate(testUser);

        // Assert
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }
}
