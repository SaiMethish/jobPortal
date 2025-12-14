package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.UserResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.mapper.UserMapper;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponseDTO register(UserRequestDTO dto) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        User user=userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user=userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user= userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponseDTO(user);
    }
}
