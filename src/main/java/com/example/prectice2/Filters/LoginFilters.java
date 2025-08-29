package com.example.prectice2.Filters;


import com.example.prectice2.DTO.JoinResponseDTO;
import com.example.prectice2.DTO.LoginRequestDTO;
import com.example.prectice2.JWT.JwtUtil;
import com.example.prectice2.UserDetails.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import java.io.IOException;

public class LoginFilters extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    public LoginFilters(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        ObjectMapper mapper = new ObjectMapper();
        LoginRequestDTO loginRequestDTO = null;
        try{
            loginRequestDTO = mapper.readValue(request.getInputStream(), LoginRequestDTO.class); // 파싱해오기
        }catch(Exception e){
            throw new AuthenticationServiceException(e.getMessage());
        }
        // 유저네임패스워드 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.username(),
                        loginRequestDTO.password());
        return authenticationManager.authenticate(authenticationToken); // 각 정보로 토큰만들어서 인증
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();
        String roles = ((CustomUserDetails) authResult.getPrincipal()).getUserRole();
        if(jwtUtil.getRefreshTokenService().getRefreshToken(username) != null){ // 이미 리프레시가 있다면 그걸 제거한뒤 진행
            jwtUtil.getRefreshTokenService().deleteRefreshToken(username);
            Cookie expireCookie = new Cookie("refreshToken", null);
		    expireCookie.setHttpOnly(true);
		    expireCookie.setPath("/");
		    expireCookie.setMaxAge(0);
		    response.addCookie(expireCookie);
        }

         JoinResponseDTO tokens = jwtUtil.generateTokens(username, roles); // 토큰 발급

        // 엑세스 토큰을 헤더로 반환
        response.setHeader("Authorization", "Bearer " + tokens.accessToken());

        // 리프레시 토큰을 HttpOnly 쿠키로 반환
        Cookie refreshCookie = new Cookie("refreshToken", tokens.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (jwtUtil.getR_EXPIRATION_TIME() / 1000));
        response.addCookie(refreshCookie);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
    }
}
