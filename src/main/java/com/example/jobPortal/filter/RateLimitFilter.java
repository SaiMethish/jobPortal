package com.example.jobPortal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_REQUESTS_PER_MINUTE = 50;

    // We only rate limit specific public endpoints
    private static final Pattern PUBLIC_API_PATTERN = Pattern.compile("^/(jobs/search|jobs/[a-fA-F0-9-]+)$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Check if the path matches rate-limited endpoints (Job details or Job search)
        if (!PUBLIC_API_PATTERN.matcher(path).matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Identify user by remote address
        String clientIp = request.getRemoteAddr();
        String redisKey = "rate_limit:" + clientIp;

        // 3. Increment Redis counter
        Long requestCount = stringRedisTemplate.opsForValue().increment(redisKey);

        if (requestCount != null && requestCount == 1L) {
            // New window started natively on first request
            stringRedisTemplate.expire(redisKey, Duration.ofMinutes(1));
        }

        // 4. Validate limit
        if (requestCount != null && requestCount > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded. Try again later.");
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
