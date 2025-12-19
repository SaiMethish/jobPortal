package com.example.jobPortal.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtil {

    public void addRefreshTokenCookie(HttpServletResponse response, String token){
        Cookie cookie=new Cookie("refresh_token",token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7*24*60*60);
        cookie.setAttribute("SameSite","None");
        response.addCookie(cookie);
    }

    public void clearRefreshToken(HttpServletResponse response){
        Cookie cookie=new Cookie("refresh_token","");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }
}
