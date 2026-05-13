package com.do_an.userservice.service;

import com.do_an.userservice.dto.request.CreateUserRequestDTO;
import com.do_an.userservice.dto.request.UpdateUserRequestDTO;
import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.entity.Role;
import com.do_an.userservice.entity.User;
import com.do_an.userservice.entity.UserRole;
import com.do_an.userservice.exceptions.UserNotFoundException;
import com.do_an.userservice.mapper.UserMapper;
import com.do_an.userservice.client.PatientServiceClient;
import com.do_an.userservice.client.DoctorServiceClient;
import com.do_an.userservice.client.LabTechnicianServiceClient;
import com.do_an.userservice.client.PharmacistServiceClient;
import com.do_an.userservice.dto.patient.PatientCreateRequest;
import com.do_an.userservice.dto.doctor.DoctorCreateRequest;
import com.do_an.userservice.dto.labtechnician.LabTechnicianCreateRequest;
import com.do_an.userservice.dto.pharmacist.PharmacistCreateRequest;
import com.do_an.userservice.repository.RoleRepository;
import com.do_an.userservice.repository.UserRepository;
import com.do_an.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PatientServiceClient patientServiceClient;
    private final DoctorServiceClient doctorServiceClient;
    private final LabTechnicianServiceClient labTechnicianServiceClient;
    private final PharmacistServiceClient pharmacistServiceClient;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO createUser(CreateUserRequestDTO request) {
        log.info("Tạo user mới với phone: {}", request.getPhone());

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại: " + request.getPhone());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullname(request.getFullName());
        user.setPhone(request.getPhone());
        user.setImageUrl(request.getImageUrl());
        user.setActive(request.isActive());

        User savedUser = userRepository.save(user);

        List<UserRole> userRoles = new ArrayList<>();
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            for (String roleName : request.getRoleNames()) {
                Role role = roleRepository.findByRoleName(roleName.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));
                UserRole userRole = new UserRole();
                userRole.setUser(savedUser);
                userRole.setRole(role);
                userRoles.add(userRole);
            }
        } else {
            Role defaultRole = roleRepository.findByRoleName("PATIENT")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò 'PATIENT' trong cơ sở dữ liệu!"));
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(defaultRole);
            userRoles.add(userRole);
        }
        
        userRoleRepository.saveAll(userRoles);

        boolean shouldCreatePatient = false;
        if (request.getRoleNames() == null || request.getRoleNames().isEmpty()) {
            shouldCreatePatient = true;
        } else if (request.getRoleNames().size() == 1 && 
                   request.getRoleNames().get(0).toUpperCase().equals("PATIENT")) {
            shouldCreatePatient = true;
        }
        
        if (shouldCreatePatient) {
            try {
                PatientCreateRequest patientRequest = PatientCreateRequest.builder()
                        .userId(savedUser.getId())
                        .contactPhone(savedUser.getPhone())
                        .build();
                patientServiceClient.createPatient(patientRequest);
                log.info("Đã tạo patient cho userId: {}", savedUser.getId());
            } catch (Exception ex) {
                log.error("Lỗi khi tạo patient cho userId {}: {}", savedUser.getId(), ex.getMessage(), ex);
            }
        }

        for (UserRole userRole : userRoles) {
            String roleName = userRole.getRole().getRoleName().toUpperCase();
            
            try {
                switch (roleName) {
                    case "PATIENT":
                        break;
                        
                    case "DOCTOR":
                        DoctorCreateRequest doctorRequest = DoctorCreateRequest.builder()
                                .userId(savedUser.getId())
                                .specializationCode("GEN") // Default specialization - General
                                .build();
                        doctorServiceClient.createDoctor(doctorRequest);
                        log.info("Đã tạo doctor cho userId: {}", savedUser.getId());
                        break;
                        
                    case "LAB_TECHNICIAN":
                        LabTechnicianCreateRequest labTechRequest = LabTechnicianCreateRequest.builder()
                                .userId(savedUser.getId())
                                .build();
                        labTechnicianServiceClient.createLabTechnician(labTechRequest);
                        log.info("Đã tạo lab technician cho userId: {}", savedUser.getId());
                        break;
                        
                    case "PHARMACIST":
                        PharmacistCreateRequest pharmacistRequest = PharmacistCreateRequest.builder()
                                .userId(savedUser.getId())
                                .degree("None") 
                                .certificate("None")
                                .build();
                        pharmacistServiceClient.createPharmacist(pharmacistRequest);
                        log.info("Đã tạo pharmacist cho userId: {}", savedUser.getId());
                        break;
                        
                    default:
                        log.debug("No additional service record needed for role: {}", roleName);
                        break;
                }
            } catch (Exception ex) {
                log.error("Lỗi khi tạo {} record cho userId {}: {}", roleName, savedUser.getId(), ex.getMessage(), ex);
            }
        }
        
        log.info("Đã tạo user thành công: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDTO updateUser(UUID userId, UpdateUserRequestDTO request) {
        log.info("Cập nhật user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi user khác");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFullName() != null) {
            user.setFullname(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getImageUrl() != null) {
            String oldAvatarUrl = user.getImageUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty() && !oldAvatarUrl.equals(request.getImageUrl())) {
                try {
                    String oldFileName = fileStorageService.extractFileNameFromUrl(oldAvatarUrl);
                    fileStorageService.deleteFile(oldFileName);
                } catch (Exception e) {
                    log.warn("Không thể xóa avatar cũ: {}", oldAvatarUrl, e);
                }
            }
            user.setImageUrl(request.getImageUrl());
        }
        
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            userRoleRepository.deleteByUser(user);

            List<UserRole> userRoles = new ArrayList<>();
            for (String roleName : request.getRoleNames()) {
                Role role = roleRepository.findByRoleName(roleName.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRoles.add(userRole);
            }
            userRoleRepository.saveAll(userRoles);
        }
        
        User savedUser = userRepository.save(user);
        log.info("Đã cập nhật user thành công: {}", userId);
        
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Xóa user (soft delete): {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // Soft delete - chỉ set isActive = false
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Đã xóa user thành công: {}", userId);
    }

    @Transactional
    public void deleteUserPermanently(UUID userId) {
        log.info("Xóa user vĩnh viễn: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));
        
        userRepository.delete(user);
        log.info("Đã xóa user vĩnh viễn thành công: {}", userId);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        log.debug("Lấy user theo ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));
        
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers(Boolean isActive, String fullName, String email) {
        log.debug("Lấy danh sách users với filters - isActive: {}, fullName: {}, email: {}", 
                isActive, fullName, email);
        
        List<User> users;
        
        if (isActive != null && fullName != null && !fullName.isEmpty()) {
            // Filter theo active và fullName
            users = userRepository.findAllByIsActive(isActive)
                    .stream()
                    .filter(user -> user.getFullname() != null && 
                            user.getFullname().toLowerCase().contains(fullName.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (isActive != null && email != null && !email.isEmpty()) {
            // Filter theo active và email
            users = userRepository.findAllByIsActive(isActive)
                    .stream()
                    .filter(user -> user.getEmail() != null && 
                            user.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (isActive != null) {
            // Filter theo active
            users = userRepository.findAllByIsActive(isActive);
        } else if (fullName != null && !fullName.isEmpty()) {
            // Filter theo fullName
            users = userRepository.findAllByFullnameContainingIgnoreCase(fullName);
        } else if (email != null && !email.isEmpty()) {
            // Filter theo email
            users = userRepository.findAllByEmailContainingIgnoreCase(email);
        } else {
            // Lấy tất cả
            users = userRepository.findAllByOrderByCreateAtDesc();
        }
        
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Lấy tất cả users");
        
        List<User> users = userRepository.findAllByOrderByCreateAtDesc();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
public UserDTO validateCredentials(String phone, String password) {

    if (phone == null || password == null) {
        throw new IllegalArgumentException("phone và password không được để trống");
    }

    log.info("Xác thực thông tin đăng nhập cho phone: {}", phone);

    User user = userRepository.findByPhone(phone)
            .orElseThrow(() ->
                    new UserNotFoundException("Không tìm thấy người dùng với phone: " + phone));

    if (!user.isActive()) {
        throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa");
    }

    // So sánh trực tiếp, không dùng hash nữa
    if (!password.equals(user.getPassword())) {
        throw new IllegalArgumentException("Mật khẩu không đúng");
    }

    log.info("Xác thực thành công cho phone: {}", phone);
    return userMapper.toDto(user);
}
    @Transactional
    public void updateUserProfile(UUID userId, Map<String, Object> userAttributes) {
        if (userAttributes == null || userAttributes.isEmpty()) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));

        if (userAttributes.containsKey("fullName")) {
            user.setFullname((String) userAttributes.get("fullName"));
        }
        if (userAttributes.containsKey("phone")) {
            user.setPhone((String) userAttributes.get("phone"));
        }
        if (userAttributes.containsKey("avatarUrl")) {
            String newAvatarUrl = (String) userAttributes.get("avatarUrl");
            String oldAvatarUrl = user.getImageUrl();

            if (newAvatarUrl != null && !newAvatarUrl.equals(oldAvatarUrl)) {

                if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                    try {
                        String oldFileName = fileStorageService.extractFileNameFromUrl(oldAvatarUrl);
                        fileStorageService.deleteFile(oldFileName);
                    } catch (Exception e) {
                        log.warn("Không thể xóa avatar cũ: {}", oldAvatarUrl, e);
                    }
                }

                user.setImageUrl(newAvatarUrl);
            }
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        log.info("Đổi mật khẩu cho user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userId));
        
        if (!user.isActive()) {
            throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Đổi mật khẩu thành công cho user: {}", userId);
    }
}
