package com.do_an.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordRequestDTO {
    
    @NotEmpty(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;
    
    @NotEmpty(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}