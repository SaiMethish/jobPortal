package com.example.jobPortal.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static boolean secure = false;
    private static String sameSite = "Lax";

    @Value("${app.cookie.secure:false}")
    public void setSecure(boolean sec) {
        secure = sec;
    }

    @Value("${app.cookie.same-site:Lax}")
    public void setSameSite(String ss) {
        sameSite = ss;
    }

    public static void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public static void clearRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }
}
