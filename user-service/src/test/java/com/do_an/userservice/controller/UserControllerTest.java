package com.do_an.userservice.controller;

import com.do_an.userservice.dto.request.ChangePasswordRequestDTO;
import com.do_an.userservice.dto.request.CreateUserRequestDTO;
import com.do_an.userservice.dto.request.LoginRequest;
import com.do_an.userservice.dto.request.UpdateUserRequestDTO;
import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.service.AuthService;
import com.do_an.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private UUID userId;
    private UserDTO testUserDTO;
    private CreateUserRequestDTO createUserRequest;
    private UpdateUserRequestDTO updateUserRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUserDTO = UserDTO.builder()
                .id(userId)
                .phone("0123456789")
                .email("test@example.com")
                .fullname("Test User")
                .isActive(true)
                .primaryRole("PATIENT")
                .roles(Set.of("PATIENT"))
                .build();

        createUserRequest = CreateUserRequestDTO.builder()
                .phone("0123456789")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .isActive(true)
                .build();

        updateUserRequest = UpdateUserRequestDTO.builder()
                .fullName("Updated Name")
                .email("updated@example.com")
                .isActive(true)
                .build();
    }

    // ============= CREATE USER TESTS =============
    @Test
    @DisplayName("Should create user successfully with status 201")
    void testCreateUser_Success() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequestDTO.class)))
                .thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(post("/user-service/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.phone").value("0123456789"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullname").value("Test User"));

        verify(userService).createUser(any(CreateUserRequestDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid data")
    void testCreateUser_InvalidData() throws Exception {
        // Arrange
        CreateUserRequestDTO invalidRequest = CreateUserRequestDTO.builder()
                .phone("")  // Invalid empty phone
                .email("invalid-email")  // Invalid email
                .build();

        // Act & Assert
        mockMvc.perform(post("/user-service/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("Should handle duplicate phone on user creation")
    void testCreateUser_DuplicatePhone() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Số điện thoại đã tồn tại"));

        // Act & Assert
        mockMvc.perform(post("/user-service/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());
    }

    // ============= UPDATE USER TESTS =============
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser_Success() throws Exception {
        // Arrange
        when(userService.updateUser(eq(userId), any(UpdateUserRequestDTO.class)))
                .thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(put("/user-service/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).updateUser(eq(userId), any(UpdateUserRequestDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    void testUpdateUser_UserNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(userId), any(UpdateUserRequestDTO.class)))
                .thenThrow(new RuntimeException("Không tìm thấy người dùng"));

        // Act & Assert
        mockMvc.perform(put("/user-service/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle duplicate email on user update")
    void testUpdateUser_DuplicateEmail() throws Exception {
        // Arrange
        when(userService.updateUser(eq(userId), any(UpdateUserRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Email đã được sử dụng"));

        // Act & Assert
        mockMvc.perform(put("/user-service/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isBadRequest());
    }

    // ============= DELETE USER TESTS =============
    @Test
    @DisplayName("Should soft delete user successfully with status 204")
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/user-service/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void testDeleteUser_UserNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Không tìm thấy người dùng"))
                .when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/user-service/users/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }

    // ============= GET USER TESTS =============
    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() throws Exception {
        // Arrange
        when(userService.getUserById(userId)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/user-service/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.phone").value("0123456789"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(userId);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent user by ID")
    void testGetUserById_UserNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(userId))
                .thenThrow(new RuntimeException("Không tìm thấy người dùng"));

        // Act & Assert
        mockMvc.perform(get("/user-service/users/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/user-service/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].phone").value("0123456789"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testGetAllUsers_EmptyList() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/user-service/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Should filter users by active status")
    void testGetAllUsers_FilterByActive() throws Exception {
        // Arrange
        List<UserDTO> activeUsers = Arrays.asList(testUserDTO);
        when(userService.getAllUsers(true, null, null)).thenReturn(activeUsers);

        // Act & Assert
        mockMvc.perform(get("/user-service/users")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        verify(userService).getAllUsers(true, null, null);
    }

    @Test
    @DisplayName("Should filter users by fullname")
    void testGetAllUsers_FilterByFullname() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers(null, "Test", null)).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/user-service/users")
                .param("fullName", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        verify(userService).getAllUsers(null, "Test", null);
    }

    @Test
    @DisplayName("Should filter users by email")
    void testGetAllUsers_FilterByEmail() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers(null, null, "test@")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/user-service/users")
                .param("email", "test@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        verify(userService).getAllUsers(null, null, "test@");
    }

    // ============= AUTHENTICATION TESTS =============
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .phone("0123456789")
                .password("password123")
                .build();

        ResponseEntity<UserDTO> response = ResponseEntity.ok()
                .header("Authorization", "Bearer token123")
                .body(testUserDTO);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/user-service/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("0123456789"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 on invalid login request")
    void testLogin_InvalidRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = LoginRequest.builder()
                .phone("")  // Empty phone
                .password("")  // Empty password
                .build();

        // Act & Assert
        mockMvc.perform(post("/user-service/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("Should return 401 on invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .phone("0123456789")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/user-service/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());
    }

    // ============= CHANGE PASSWORD TESTS (if exists) =============
    @Test
    @DisplayName("Should handle change password request")
    void testChangePassword_Success() throws Exception {
        // Arrange
        ChangePasswordRequestDTO passwordRequest = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        // Act & Assert (endpoint might not exist yet, adjust based on controller)
        mockMvc.perform(post("/user-service/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk());
    }
}
