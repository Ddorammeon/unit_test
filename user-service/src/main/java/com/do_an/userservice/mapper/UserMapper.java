package com.do_an.userservice.mapper;

import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.entity.User;
import com.do_an.userservice.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(source = "id", target = "id")
    @Mapping(source = "userRoles", target = "roles", qualifiedByName = "mapUserRolesToStringSet")
    @Mapping(source = ".", target = "primaryRole", qualifiedByName = "extractPrimaryRole")
    UserDTO toDto(User user);


    List<UserDTO> toDtoList(List<User> users);

    /**
     * Hàm helper tùy chỉnh:
     * Chuyển List<UserRole> (Entity) -> Set<String> (Tên Role)
     */
    @Named("mapUserRolesToStringSet")
    default Set<String> mapUserRolesToStringSet(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Set.of(); // Trả về Set rỗng
        }

        return userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toSet());
    }
    
    /**
     * Trích xuất vai trò chính từ User
     * Ưu tiên: DOCTOR > ADMIN > PHARMACIST > LAB_TECHNICIAN > RECEPTIONIST > PATIENT
     */
    @Named("extractPrimaryRole")
    default String extractPrimaryRole(User user) {
        if (user == null || user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return null;
        }
        
        Set<String> roleNames = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toSet());
        
        // Ưu tiên theo thứ tự
        if (roleNames.contains("DOCTOR")) return "DOCTOR";
        if (roleNames.contains("ADMIN")) return "ADMIN";
        if (roleNames.contains("PHARMACIST")) return "PHARMACIST";
        if (roleNames.contains("LAB_TECHNICIAN")) return "LAB_TECHNICIAN";
        if (roleNames.contains("RECEPTIONIST")) return "RECEPTIONIST";
        if (roleNames.contains("PATIENT")) return "PATIENT";
        
        // Nếu không có role nào khớp, lấy role đầu tiên
        return roleNames.iterator().next();
    }
}