package com.example.jobPortal.controller;


import com.example.jobPortal.dto.request.LoginRequest;
import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.AuthResponse;
import com.example.jobPortal.exception.UnauthorizedException;
import com.example.jobPortal.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(allowCredentials = "true",origins = "http://localhost:3000")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody UserRequestDTO userRequestDTO, HttpServletResponse response){
        return authService.register(userRequestDTO,response);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest dto, HttpServletResponse response){
        return authService.login(dto,response);
    }

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

    @GetMapping("oauth2/authorize/google")
    public void googleRedirect(HttpServletResponse response) throws IOException{
        response.sendRedirect("/oauth2/authorization/google");
    }

}
