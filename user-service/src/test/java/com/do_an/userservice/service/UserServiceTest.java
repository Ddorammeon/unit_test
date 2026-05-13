package com.do_an.userservice.service;

import com.do_an.userservice.client.DoctorServiceClient;
import com.do_an.userservice.client.LabTechnicianServiceClient;
import com.do_an.userservice.client.PatientServiceClient;
import com.do_an.userservice.client.PharmacistServiceClient;
import com.do_an.userservice.dto.doctor.DoctorCreateRequest;
import com.do_an.userservice.dto.labtechnician.LabTechnicianCreateRequest;
import com.do_an.userservice.dto.patient.PatientCreateRequest;
import com.do_an.userservice.dto.pharmacist.PharmacistCreateRequest;
import com.do_an.userservice.dto.request.CreateUserRequestDTO;
import com.do_an.userservice.dto.request.UpdateUserRequestDTO;
import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.entity.Role;
import com.do_an.userservice.entity.User;
import com.do_an.userservice.entity.UserRole;
import com.do_an.userservice.entity.UserRoleId;
import com.do_an.userservice.exceptions.UserNotFoundException;
import com.do_an.userservice.mapper.UserMapper;
import com.do_an.userservice.repository.RoleRepository;
import com.do_an.userservice.repository.UserRepository;
import com.do_an.userservice.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private DoctorServiceClient doctorServiceClient;

    @Mock
    private LabTechnicianServiceClient labTechnicianServiceClient;

    @Mock
    private PharmacistServiceClient pharmacistServiceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User testUser;
    private UserDTO testUserDTO;
    private Role patientRole;
    private Role doctorRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Setup test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setPhone("0123456789");
        testUser.setEmail("test@example.com");
        testUser.setFullname("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
        testUser.setImageUrl("http://example.com/image.jpg");
        testUser.setCreateAt(LocalDateTime.now());

        // Setup test UserDTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(userId);
        testUserDTO.setPhone("0123456789");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFullname("Test User");
        testUserDTO.setActive(true);
        testUserDTO.setImageUrl("http://example.com/image.jpg");

        // Setup test roles
        patientRole = new Role();
        patientRole.setId(UUID.randomUUID());
        patientRole.setRoleName("PATIENT");

        doctorRole = new Role();
        doctorRole.setId(UUID.randomUUID());
        doctorRole.setRoleName("DOCTOR");
    }

    // ============= CREATE USER TESTS =============
    @Test
    @DisplayName("Should create user successfully with default PATIENT role")
    void testCreateUser_Success_DefaultPatientRole() {
        // Arrange
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .phone("0123456789")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .isActive(true)
                .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("0123456789", result.getPhone());
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).saveAll(anyList());
        verify(patientServiceClient).createPatient(any(PatientCreateRequest.class));
    }

    @Test
    @DisplayName("Should create user with multiple roles")
    void testCreateUser_Success_MultipleRoles() {
        // Arrange
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .phone("0987654321")
                .email("doctor@example.com")
                .password("password123")
                .fullName("Doctor User")
                .roleNames(Arrays.asList("DOCTOR"))
                .isActive(true)
                .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("DOCTOR")).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        verify(doctorServiceClient).createDoctor(any(DoctorCreateRequest.class));
        verify(patientServiceClient, never()).createPatient(any());
    }

    @Test
    @DisplayName("Should throw exception when phone already exists")
    void testCreateUser_PhoneDuplicate() {
        // Arrange
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .phone("0123456789")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreateUser_EmailDuplicate() {
        // Arrange
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .phone("0123456789")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    @DisplayName("Should throw exception when role not found")
    void testCreateUser_RoleNotFound() {
        // Arrange
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .phone("0123456789")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .roleNames(Arrays.asList("INVALID_ROLE"))
                .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.createUser(request));
    }

    // ============= UPDATE USER TESTS =============
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser_Success() {
        // Arrange
        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .fullName("Updated Name")
                .email("newemail@example.com")
                .isActive(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(userId, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate email")
    void testUpdateUser_DuplicateEmail() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("existing@example.com");

        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    @DisplayName("Should handle old avatar deletion during update")
    void testUpdateUser_DeleteOldAvatar() {
        // Arrange
        testUser.setImageUrl("http://example.com/old-image.jpg");
        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .imageUrl("http://example.com/new-image.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(fileStorageService.extractFileNameFromUrl(testUser.getImageUrl()))
                .thenReturn("old-image.jpg");
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(userId, request);

        // Assert
        assertNotNull(result);
        verify(fileStorageService).extractFileNameFromUrl("http://example.com/old-image.jpg");
        verify(fileStorageService).deleteFile("old-image.jpg");
    }

    @Test
    @DisplayName("Should throw exception when user not found on update")
    void testUpdateUser_UserNotFound() {
        // Arrange
        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .fullName("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, request));
    }

    // ============= DELETE USER TESTS =============
    @Test
    @DisplayName("Should soft delete user successfully")
    void testDeleteUser_SoftDelete_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).save(any(User.class));
        assertTrue(!testUser.isActive());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void testDeleteUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    @DisplayName("Should permanently delete user")
    void testDeleteUserPermanently_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUserPermanently(userId);

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should throw exception when permanently deleting non-existent user")
    void testDeleteUserPermanently_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUserPermanently(userId));
    }

    // ============= GET USER TESTS =============
    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void testGetUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        List<UserDTO> userDTOs = Arrays.asList(testUserDTO);

        when(userRepository.findAllByOrderByCreateAtDesc()).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAllByOrderByCreateAtDesc();
    }

    @Test
    @DisplayName("Should filter users by active status")
    void testGetAllUsers_FilterByActive() {
        // Arrange
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findAllByIsActive(true)).thenReturn(activeUsers);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers(true, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAllByIsActive(true);
    }

    @Test
    @DisplayName("Should filter users by fullname")
    void testGetAllUsers_FilterByFullname() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAllByFullnameContainingIgnoreCase("Test")).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers(null, "Test", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAllByFullnameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Should filter users by email")
    void testGetAllUsers_FilterByEmail() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAllByEmailContainingIgnoreCase("test@")).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers(null, null, "test@");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAllByEmailContainingIgnoreCase("test@");
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void testGetAllUsers_EmptyResult() {
        // Arrange
        when(userRepository.findAllByOrderByCreateAtDesc()).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
