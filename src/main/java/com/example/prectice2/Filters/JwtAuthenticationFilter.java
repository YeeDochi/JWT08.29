package com.example.prectice2.Filters;

import com.example.prectice2.JWT.JwtUtil;
import com.example.prectice2.UserDetails.CustomUserDetailService;

import io.micrometer.common.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 기본 인증 필터

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailService userDetailService) {
        this.jwtUtil = jwtUtil;
        this.userDetailService = userDetailService;
    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) { // 엑세스토큰이 있는가?
            token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) { // 엑세스토큰이 유효한가?
                username = jwtUtil.getUsername(token);
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication); // 유효하다면 유저정보를 토큰으로 시큐리티에 넘긴다.
            }
        }
        filterChain.doFilter(request, response);
    }
}
