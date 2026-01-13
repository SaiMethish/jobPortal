package com.example.jobPortal.security;

import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User= (CustomOAuth2User) authentication.getPrincipal();
        User user=customOAuth2UserService.processOAuthUser(customOAuth2User);

        String accessToken=jwtUtil.generateAccessToken(user.getEmail(),user.getUserId(),user.getRole());
        String refreshToken=jwtUtil.generateRefreshToken(user.getEmail());

        CookieUtil.addRefreshTokenCookie(response,refreshToken);

        try{
            response.sendRedirect("http://localhost:3000/oauth-success?access="+accessToken);
        }
        catch (Exception e){
            throw new RuntimeException("Oauth redirect failed");
        }
    }
}
