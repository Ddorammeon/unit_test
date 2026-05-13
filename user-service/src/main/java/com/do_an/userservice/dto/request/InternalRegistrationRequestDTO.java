package com.do_an.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InternalRegistrationRequestDTO {
    @NotEmpty @Email
    private String email;
    @NotEmpty
    private String hashedPassword;
    @NotEmpty
    private String fullName;
    @NotEmpty
    private String phone;
}
