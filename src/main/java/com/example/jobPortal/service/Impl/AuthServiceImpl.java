package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.LoginRequest;
import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.AuthResponse;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.EmailAlreadyExistsException;
import com.example.jobPortal.exception.InvalidTokenException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.mapper.UserMapper;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.security.JwtUtil;
import com.example.jobPortal.service.AuthService;
import com.example.jobPortal.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    public AuthResponse register(UserRequestDTO dto, HttpServletResponse response) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("user already registered");
        }
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user = repo.save(user);
        if (user.getRole() == com.example.jobPortal.entity.Role.CANDIDATE) {
            CandidateProfile candidateProfile = new CandidateProfile();
            candidateProfile.setUser(user);
            candidateProfile.setFullName("");
            candidateProfileRepository.save(candidateProfile);
        }

        String access = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail());

        CookieUtil.addRefreshTokenCookie(response, refresh);

        return AuthResponse.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .accessToken(access)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest dto, HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto
                .getPassword()));
        User user = repo.findByEmail(dto.getEmail()).orElseThrow(() -> new ResourceNotFoundException("user not found"));
        String access = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail());

        CookieUtil.addRefreshTokenCookie(response, refresh);

        return AuthResponse.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .accessToken(access)
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .build();
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid Refresh Token");
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = repo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("user not found"));
        String newAccess = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        return AuthResponse.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .accessToken(newAccess)
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .build();
    }
}
