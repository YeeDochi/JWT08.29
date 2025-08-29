package com.example.prectice2.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.prectice2.DTO.LoginRequestDTO;
import com.example.prectice2.JWT.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final JwtUtil jwtUtil;

    public String login(LoginRequestDTO loginRequestDTO) {
        // TODO: Implement login logic
        return "로그인 성공";
    }

    public ResponseEntity<?> logout(String username, HttpServletResponse response) {
        jwtUtil.getRefreshTokenService().deleteRefreshToken(username);
        Cookie cookie = new Cookie("refreshtoken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        return ResponseEntity.ok("로그아웃 성공");
    }

}
