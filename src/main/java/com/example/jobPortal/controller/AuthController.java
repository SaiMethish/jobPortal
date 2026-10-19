package com.example.jobPortal.controller;


import com.example.jobPortal.dto.request.LoginRequest;
import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.request.ResetPasswordRequest;
import com.example.jobPortal.dto.response.AuthResponse;
import com.example.jobPortal.exception.UnauthorizedException;
import com.example.jobPortal.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Authentication APIs", description = "Endpoints for user registration, local login, JWT token refresh, and OAuth flows.")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(allowCredentials = "true",origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register User", description = "Creates a new User account mapping locally returning Authentication tokens.")
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody UserRequestDTO userRequestDTO, HttpServletResponse response){
        return authService.register(userRequestDTO,response);
    }

    @Operation(summary = "Login Local Account", description = "Executes email/password verification allocating new JWT access and HttpOnly refresh cookies.")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest dto, HttpServletResponse response){
        return authService.login(dto,response);
    }

    @Operation(summary = "Refresh JWT Identity", description = "Consumes HttpOnly 'refresh_token' returning a structurally renewed pair of JWT security cookies/headers.")
    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request){
        String refreshToken=null;
        if(request.getCookies()!=null){
            for(Cookie c:request.getCookies()){
                if(c.getName().equals("refresh_token")){
                    refreshToken=c.getValue();
                }
            }
        }
        if(refreshToken==null) throw new UnauthorizedException("Missing refresh Token");
        return authService.refresh(refreshToken);
    }

    @Operation(summary = "Google OAuth Request", description = "Redirects users to Google OAuth. Pass ?role=CANDIDATE or ?role=RECRUITER to set the role for new accounts.")
    @GetMapping("oauth2/authorize/google")
    public void googleRedirect(@RequestParam(required = false) String role,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        // Store the desired role in the session — OAuthSuccessHandler will read it after Google returns
        if (role != null && !role.isEmpty()) {
            request.getSession().setAttribute("oauth_desired_role", role.toUpperCase());
        } else {
            request.getSession().removeAttribute("oauth_desired_role");
        }
        response.sendRedirect("/oauth2/authorization/google");
    }

    @Operation(summary = "Logout User", description = "Clears the refresh_token cookie.")
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Should match environment
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Operation(summary = "Reset Password", description = "Directly resets the user password (simplified flow without email token).")
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest dto) {
        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
    }

    @Operation(summary = "Change Password", description = "Changes the authenticated user's password verifying the current password.")
    @PostMapping("/change-password")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public void changePassword(
            @Valid @RequestBody com.example.jobPortal.dto.request.ChangePasswordRequest dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        authService.changePassword(userDetails.getUsername(), dto.getCurrentPassword(), dto.getNewPassword());
    }
}
