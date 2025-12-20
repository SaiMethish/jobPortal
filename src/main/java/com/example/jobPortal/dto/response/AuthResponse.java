package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.AuthProvider;
import com.example.jobPortal.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID userId;
    private String email;
    private Role role;
    private AuthProvider authProvider;
    private String accessToken;
}
