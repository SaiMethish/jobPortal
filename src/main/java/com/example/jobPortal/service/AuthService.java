package com.example.jobPortal.service;


import com.example.jobPortal.dto.request.LoginRequest;
import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(UserRequestDTO dto, HttpServletResponse response);
    AuthResponse login(LoginRequest dto, HttpServletResponse response);
    AuthResponse refresh(String refreshToken);
    
    void resetPassword(String email, String newPassword);
}
