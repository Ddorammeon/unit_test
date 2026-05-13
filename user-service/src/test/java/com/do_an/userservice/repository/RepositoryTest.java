package com.do_an.userservice.repository;

import com.do_an.userservice.entity.Role;
import com.do_an.userservice.entity.User;
import com.do_an.userservice.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User testUser;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        // Create test role
        patientRole = new Role();
        patientRole.setRoleName("PATIENT");
        roleRepository.save(patientRole);

        // Create test user
        testUser = new User();
        testUser.setPhone("0123456789");
        testUser.setEmail("test@example.com");
        testUser.setFullname("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
        testUser.setCreateAt(LocalDateTime.now());
        userRepository.save(testUser);
    }

    // ============= FIND BY PHONE TESTS =============
    @Test
    @DisplayName("Should find user by phone")
    void testFindByPhone_Success() {
        // Act
        Optional<User> foundUser = userRepository.findByPhone("0123456789");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("0123456789", foundUser.get().getPhone());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when phone not found")
    void testFindByPhone_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByPhone("9999999999");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // ============= FIND BY EMAIL TESTS =============
    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail_Success() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("0123456789", foundUser.get().getPhone());
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // ============= FIND BY ACTIVE STATUS TESTS =============
    @Test
    @DisplayName("Should find all active users")
    void testFindAllByIsActive_Active() {
        // Arrange
        User inactiveUser = new User();
        inactiveUser.setPhone("9876543210");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setFullname("Inactive User");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setActive(false);
        inactiveUser.setCreateAt(LocalDateTime.now());
        userRepository.save(inactiveUser);

        // Act
        List<User> activeUsers = userRepository.findAllByIsActive(true);

        // Assert
        assertEquals(1, activeUsers.size());
        assertEquals("0123456789", activeUsers.get(0).getPhone());
        assertTrue(activeUsers.get(0).isActive());
    }

    @Test
    @DisplayName("Should find all inactive users")
    void testFindAllByIsActive_Inactive() {
        // Arrange
        User inactiveUser = new User();
        inactiveUser.setPhone("9876543210");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setFullname("Inactive User");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setActive(false);
        inactiveUser.setCreateAt(LocalDateTime.now());
        userRepository.save(inactiveUser);

        // Act
        List<User> inactiveUsers = userRepository.findAllByIsActive(false);

        // Assert
        assertEquals(1, inactiveUsers.size());
        assertEquals("9876543210", inactiveUsers.get(0).getPhone());
        assertFalse(inactiveUsers.get(0).isActive());
    }

    // ============= FIND BY FULLNAME TESTS =============
    @Test
    @DisplayName("Should find users by fullname containing search term")
    void testFindAllByFullnameContainingIgnoreCase() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setPhone("1111111111");
        anotherUser.setEmail("another@example.com");
        anotherUser.setFullname("Another User");
        anotherUser.setPassword("encodedPassword");
        anotherUser.setActive(true);
        anotherUser.setCreateAt(LocalDateTime.now());
        userRepository.save(anotherUser);

        // Act
        List<User> foundUsers = userRepository.findAllByFullnameContainingIgnoreCase("User");

        // Assert
        assertEquals(2, foundUsers.size());
    }

    @Test
    @DisplayName("Should find users by fullname case insensitive")
    void testFindAllByFullnameContainingIgnoreCase_CaseInsensitive() {
        // Act
        List<User> foundUsers = userRepository.findAllByFullnameContainingIgnoreCase("test");

        // Assert
        assertEquals(1, foundUsers.size());
        assertEquals("Test User", foundUsers.get(0).getFullname());
    }

    @Test
    @DisplayName("Should return empty when fullname not found")
    void testFindAllByFullnameContainingIgnoreCase_NotFound() {
        // Act
        List<User> foundUsers = userRepository.findAllByFullnameContainingIgnoreCase("NonExistent");

        // Assert
        assertTrue(foundUsers.isEmpty());
    }

    // ============= FIND BY EMAIL CONTAINING TESTS =============
    @Test
    @DisplayName("Should find users by email containing search term")
    void testFindAllByEmailContainingIgnoreCase() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setPhone("1111111111");
        anotherUser.setEmail("another@example.com");
        anotherUser.setFullname("Another User");
        anotherUser.setPassword("encodedPassword");
        anotherUser.setActive(true);
        anotherUser.setCreateAt(LocalDateTime.now());
        userRepository.save(anotherUser);

        // Act
        List<User> foundUsers = userRepository.findAllByEmailContainingIgnoreCase("@example.com");

        // Assert
        assertEquals(2, foundUsers.size());
    }

    @Test
    @DisplayName("Should find users by email case insensitive")
    void testFindAllByEmailContainingIgnoreCase_CaseInsensitive() {
        // Act
        List<User> foundUsers = userRepository.findAllByEmailContainingIgnoreCase("TEST");

        // Assert
        assertEquals(1, foundUsers.size());
    }

    // ============= FIND ALL ORDERED TESTS =============
    @Test
    @DisplayName("Should find all users ordered by create time descending")
    void testFindAllByOrderByCreateAtDesc() {
        // Arrange
        User newUser = new User();
        newUser.setPhone("2222222222");
        newUser.setEmail("new@example.com");
        newUser.setFullname("New User");
        newUser.setPassword("encodedPassword");
        newUser.setActive(true);
        newUser.setCreateAt(LocalDateTime.now().plusHours(1));
        userRepository.save(newUser);

        // Act
        List<User> users = userRepository.findAllByOrderByCreateAtDesc();

        // Assert
        assertEquals(2, users.size());
        assertEquals("new@example.com", users.get(0).getEmail());
    }

    @Test
    @DisplayName("Should find all users ordered by create time ascending")
    void testFindAllByOrderByCreateAtAsc() {
        // Arrange
        User newUser = new User();
        newUser.setPhone("2222222222");
        newUser.setEmail("new@example.com");
        newUser.setFullname("New User");
        newUser.setPassword("encodedPassword");
        newUser.setActive(true);
        newUser.setCreateAt(LocalDateTime.now().plusHours(1));
        userRepository.save(newUser);

        // Act
        List<User> users = userRepository.findAllByOrderByCreateAtAsc();

        // Assert
        assertEquals(2, users.size());
        assertEquals("test@example.com", users.get(0).getEmail());
    }
}

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Role patientRole = new Role();
        patientRole.setRoleName("PATIENT");
        roleRepository.save(patientRole);

        Role doctorRole = new Role();
        doctorRole.setRoleName("DOCTOR");
        roleRepository.save(doctorRole);
    }

    @Test
    @DisplayName("Should find role by name")
    void testFindByRoleName_Success() {
        // Act
        Optional<Role> foundRole = roleRepository.findByRoleName("PATIENT");

        // Assert
        assertTrue(foundRole.isPresent());
        assertEquals("PATIENT", foundRole.get().getRoleName());
    }

    @Test
    @DisplayName("Should return empty when role name not found")
    void testFindByRoleName_NotFound() {
        // Act
        Optional<Role> foundRole = roleRepository.findByRoleName("ADMIN");

        // Assert
        assertFalse(foundRole.isPresent());
    }

    @Test
    @DisplayName("Should check if role exists by ID")
    void testExistsById_True() {
        // Arrange
        Role role = roleRepository.findByRoleName("PATIENT").get();

        // Act
        boolean exists = roleRepository.existsById(role.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when role ID does not exist")
    void testExistsById_False() {
        // Act
        boolean exists = roleRepository.existsById(UUID.randomUUID());

        // Assert
        assertFalse(exists);
    }
}

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("UserRoleRepository Tests")
class UserRoleRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create test role
        testRole = new Role();
        testRole.setRoleName("PATIENT");
        roleRepository.save(testRole);

        // Create test user
        testUser = new User();
        testUser.setPhone("0123456789");
        testUser.setEmail("test@example.com");
        testUser.setFullname("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
        testUser.setCreateAt(LocalDateTime.now());
        userRepository.save(testUser);

        // Create UserRole relationship
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRoleRepository.save(userRole);
    }

    @Test
    @DisplayName("Should find user roles by user")
    void testFindByUser_Success() {
        // Act
        List<UserRole> userRoles = userRoleRepository.findByUser(testUser);

        // Assert
        assertEquals(1, userRoles.size());
        assertEquals("PATIENT", userRoles.get(0).getRole().getRoleName());
    }

    @Test
    @DisplayName("Should find user roles by user ID")
    void testFindByUserId_Success() {
        // Act
        List<UserRole> userRoles = userRoleRepository.findByUserId(testUser.getId());

        // Assert
        assertEquals(1, userRoles.size());
        assertEquals(testUser.getId(), userRoles.get(0).getUser().getId());
    }

    @Test
    @DisplayName("Should find user roles by role")
    void testFindByRole_Success() {
        // Act
        List<UserRole> userRoles = userRoleRepository.findByRole(testRole);

        // Assert
        assertEquals(1, userRoles.size());
        assertEquals("PATIENT", userRoles.get(0).getRole().getRoleName());
    }

    @Test
    @DisplayName("Should check if user role exists")
    void testExistsByUserAndRole_True() {
        // Act
        boolean exists = userRoleRepository.existsByUserAndRole(testUser, testRole);

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when user role does not exist")
    void testExistsByUserAndRole_False() {
        // Arrange
        Role anotherRole = new Role();
        anotherRole.setRoleName("DOCTOR");
        roleRepository.save(anotherRole);

        // Act
        boolean exists = userRoleRepository.existsByUserAndRole(testUser, anotherRole);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should delete user role by user and role")
    void testDeleteByUserAndRole_Success() {
        // Act
        userRoleRepository.deleteByUserAndRole(testUser, testRole);
        boolean exists = userRoleRepository.existsByUserAndRole(testUser, testRole);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should delete all user roles by user")
    void testDeleteByUser_Success() {
        // Act
        userRoleRepository.deleteByUser(testUser);
        List<UserRole> userRoles = userRoleRepository.findByUser(testUser);

        // Assert
        assertTrue(userRoles.isEmpty());
    }

    @Test
    @DisplayName("Should find users by role name using custom query")
    void testFindUsersByRoleName_Success() {
        // Act
        List<User> users = userRoleRepository.findUsersByRoleName("PATIENT");

        // Assert
        assertEquals(1, users.size());
        assertEquals("0123456789", users.get(0).getPhone());
    }

    @Test
    @DisplayName("Should find roles by user ID using custom query")
    void testFindRolesByUserId_Success() {
        // Act
        List<Role> roles = userRoleRepository.findRolesByUserId(testUser.getId());

        // Assert
        assertEquals(1, roles.size());
        assertEquals("PATIENT", roles.get(0).getRoleName());
    }
}
