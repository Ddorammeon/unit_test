package com.do_an.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String email;
    private String fullname;
    private String phone;
    private boolean isActive;
    private String imageUrl;
    private LocalDateTime createAt;

    private Set<String> roles;

    private String primaryRole;
}
