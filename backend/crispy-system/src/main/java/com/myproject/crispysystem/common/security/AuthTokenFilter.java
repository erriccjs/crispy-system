package com.myproject.crispysystem.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.crispysystem.users.payload.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Skip filtering for login endpoint
        if ("/api/login".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = extractToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getSubjectFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else if (token == null) {
                handleError(response, "Token is missing", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                handleError(response, "Invalid or expired token", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }catch (Exception e){
            logger.error("Cannot set user authentication: {}", e.getMessage());
            handleError(response, "Authentication failed: " + e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return null;
    }

    private void handleError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");

        ApiResponse<?> apiResponse = new ApiResponse<>(false,message);
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }

}


