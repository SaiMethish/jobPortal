package com.example.jobPortal.service;

import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO register(UserRequestDTO dto);
    UserResponseDTO getUserByEmail(String email);
}
