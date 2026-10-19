package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.AuthProvider;
import com.example.jobPortal.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserDTO implements Serializable {
    private UUID userId;
    private String email;
    private Role role;
    private AuthProvider authProvider;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
