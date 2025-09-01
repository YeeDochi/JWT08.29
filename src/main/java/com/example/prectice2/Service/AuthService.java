package com.example.prectice2.Service;

import com.example.prectice2.DTO.LoginResponseDTO;
import com.example.prectice2.JWT.JwtUtil;
import com.example.prectice2.User.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	public AuthService(JwtUtil jwtUtil, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
	}

	// 리프레시 토큰 사용한 재발급 처리
	public void handleReissue(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String requestRefreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    requestRefreshToken = cookie.getValue();
                    break;
                }
            }
        }
    	String username = jwtUtil.R_getUsername(requestRefreshToken);
    	// Redis에서 토큰 조회 및 비교
    	String redisRefreshToken = jwtUtil.getRefreshTokenService().getRefreshToken(username);
    	System.out.println("Redis Token: " + redisRefreshToken+"\n Real Token: " + requestRefreshToken);
    	if (requestRefreshToken == null || !requestRefreshToken.equals(redisRefreshToken)) {
        	throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
    	}

    	// 만료 여부 체크
    	if (!jwtUtil.validateRefreshToken(requestRefreshToken)) {
        	jwtUtil.getRefreshTokenService().deleteRefreshToken(username);
        	throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
    	}

        //새로운 토큰 발급
		jwtUtil.getRefreshTokenService().deleteRefreshToken(username); // 기존 리프레시 토큰 삭제
		String role = userRepository.findByUsername(username).map(u -> u.getRole()).orElse(null);
		LoginResponseDTO tokens = jwtUtil.generateTokens(username, role); // 새로운 토큰 생성(리프레시 토큰 생성 파트에 redis에 저장됨)

		// 액세스 토큰을 헤더로 반환
		response.setHeader("Authorization", "Bearer " + tokens.accessToken());

		// 기존 refreshToken 쿠키 만료
		Cookie expireCookie = new Cookie("refreshToken", null);
		expireCookie.setHttpOnly(true);
		expireCookie.setPath("/");
		expireCookie.setMaxAge(0);
		response.addCookie(expireCookie);

		// 새 리프레시 토큰을 HttpOnly 쿠키로 반환
		Cookie refreshCookie = new Cookie("refreshToken", tokens.refreshToken());
		refreshCookie.setHttpOnly(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge((int) (jwtUtil.getR_EXPIRATION_TIME() / 1000));
		response.addCookie(refreshCookie);
	}
}
