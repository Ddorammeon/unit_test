package com.do_an.userservice.controller;

import com.do_an.userservice.dto.request.ChangePasswordRequestDTO;
import com.do_an.userservice.dto.request.CreateUserRequestDTO;
import com.do_an.userservice.dto.request.LoginRequest;
import com.do_an.userservice.dto.request.UpdateUserRequestDTO;
import com.do_an.userservice.dto.response.UserDTO;
import com.do_an.userservice.service.AuthService;
import com.do_an.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-service/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API quản lý người dùng cơ bản")
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;

    @Operation(
            summary = "Tạo User mới",
            description = "Tạo một user mới trong hệ thống. Mặc định sẽ gán role PATIENT nếu không chỉ định role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo user thành công",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Số điện thoaị hoặc Email đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    //@SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "Thông tin user cần tạo", required = true)
            @Valid @RequestBody CreateUserRequestDTO request) {
        log.info("Nhận request tạo user mới");
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(
            summary = "Cập nhật User",
            description = "Cập nhật thông tin user. Các trường không gửi lên sẽ giữ nguyên giá trị cũ."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Email đã được sử dụng bởi user khác"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    //@SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID của User", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Thông tin cần cập nhật", required = true)
            @Valid @RequestBody UpdateUserRequestDTO request) {
        log.info("Nhận request cập nhật user: {}", userId);
        UserDTO user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Xóa User (soft delete)",
            description = "Xóa user bằng cách set isActive = false. User sẽ không thể đăng nhập nhưng dữ liệu vẫn được giữ lại."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    //@SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID của User", required = true)
            @PathVariable UUID userId) {
        log.info("Nhận request xóa user: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy User theo ID",
            description = "Lấy thông tin chi tiết của một user theo User ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy user",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    //@SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID của User", required = true)
            @PathVariable UUID userId) {
        log.info("Nhận request lấy user theo ID: {}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Lấy danh sách Users với filter",
            description = "Lấy danh sách users với các filter: isActive, fullName, email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    //@SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @Parameter(description = "Trạng thái active (true/false)", required = false)
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Tên đầy đủ (tìm kiếm không phân biệt hoa thường)", required = false)
            @RequestParam(required = false) String fullName,
            @Parameter(description = "Email (tìm kiếm không phân biệt hoa thường)", required = false)
            @RequestParam(required = false) String email) {
        log.info("Nhận request lấy danh sách users với filters");
        List<UserDTO> users = userService.getAllUsers(isActive, fullName, email);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest credentials) {
        return authService.login(credentials);
    }

    @Operation(
            summary = "Đổi mật khẩu",
            description = "Đổi mật khẩu của user. Yêu cầu cung cấp mật khẩu cũ và mật khẩu mới."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID của User", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Thông tin đổi mật khẩu (mật khẩu cũ và mới)", required = true)
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}
