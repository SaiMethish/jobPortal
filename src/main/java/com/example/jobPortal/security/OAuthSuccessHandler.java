package com.example.jobPortal.security;

import com.example.jobPortal.entity.Role;
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

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // Read the desired role stored by AuthController before the OAuth redirect
        String roleStr = (String) request.getSession().getAttribute("oauth_desired_role");
        Role desiredRole = null;
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                desiredRole = Role.valueOf(roleStr);
            } catch (IllegalArgumentException ignored) {}
        }
        request.getSession().removeAttribute("oauth_desired_role"); // clean up

        User user;
        try {
            user = customOAuth2UserService.processOAuthUser(customOAuth2User, desiredRole);
        } catch (RuntimeException e) {
            response.sendRedirect(frontendUrl + "/auth/login?error=account_not_found");
            return;
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        CookieUtil.addRefreshTokenCookie(response, refreshToken);

        try {
            response.sendRedirect(frontendUrl + "/oauth-success?access=" + accessToken);
        } catch (Exception e) {
            throw new RuntimeException("Oauth redirect failed");
        }
    }
}
