package com.do_an.userservice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateUserRequestDTO {
    private String email;
    
    private String fullName;
    
    private String phone;
    
    private String imageUrl;
    
    private Boolean isActive;
    
    private List<String> roleNames;
}
