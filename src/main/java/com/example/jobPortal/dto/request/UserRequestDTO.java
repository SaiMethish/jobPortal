package com.example.jobPortal.dto.request;

import com.example.jobPortal.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDTO {
    private String email;
    private String password;
    private Role role;
}
