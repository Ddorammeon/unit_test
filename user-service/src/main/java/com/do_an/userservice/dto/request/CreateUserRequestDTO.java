package com.do_an.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateUserRequestDTO {
    
    @NotEmpty
    @Email
    private String email;
    
    @NotEmpty
    private String password; 
    
    @NotEmpty
    private String fullName;
    @NotEmpty
    private String phone;
    
    private String imageUrl;
    
    @Builder.Default
    private boolean isActive = true;
    
    private List<String> roleNames;
}
