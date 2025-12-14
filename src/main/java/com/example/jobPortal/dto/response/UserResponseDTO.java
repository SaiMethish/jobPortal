package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.AuthProvider;
import com.example.jobPortal.entity.Role;

import java.util.UUID;

public class UserResponseDTO {
    private UUID userId;
    private String email;
    private Role role;
    private AuthProvider authProvider;
    private boolean isVerified;
}
