package com.example.prectice2.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.prectice2.DTO.JoinResponseDTO;
import com.example.prectice2.DTO.LoginRequestDTO;

import java.security.Key;
import java.util.Date;

@Component
@Getter
public class JwtUtil {

	private final String SECRET_KEY;
	private final long EXPIRATION_TIME;
	private final Key key;

    private final String R_SECRET_KEY;
	private final long R_EXPIRATION_TIME;
	private final Key R_key;

    private final RefreshTokenService refreshTokenService;

    @Autowired
	public JwtUtil( // 환경변수 세팅
        @Value("${spring.jwt.secret}") String secretKey,
		@Value("${spring.jwt.expiration}") long expirationTime,
		@Value("${spring.jwt_refresh.secret}") String r_secretKey,
		@Value("${spring.jwt_refresh.expiration}") long r_expirationTime,
        RefreshTokenService refreshTokenService
    )
    {
        this.refreshTokenService = refreshTokenService;
		this.SECRET_KEY = secretKey;
		this.EXPIRATION_TIME = expirationTime;
		this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

		this.R_SECRET_KEY = r_secretKey;
		this.R_EXPIRATION_TIME = r_expirationTime;
		this.R_key = Keys.hmacShaKeyFor(R_SECRET_KEY.getBytes());
	}


	// 토큰 생성 (role 포함)
	public String generateToken(String username, String role) {
		return Jwts.builder()
				.setSubject(username)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
    // 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + R_EXPIRATION_TIME))
                .signWith(R_key, SignatureAlgorithm.HS256)
                .compact();

        refreshTokenService.setRefreshToken(username, token);
        return token;
    }

	// 토큰에서 username 추출
	public String getUsername(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	// 토큰에서 role 추출 (리프레시는 role 없음)
	public String getRole(String token) {
		return (String) Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("role");
	}

    	// 리프레시 토큰에서 username 추출
	public String R_getUsername(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(R_key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}


	// 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
    public boolean validateRefreshToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(R_key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
    public JoinResponseDTO generateTokens(String username, String roles){
        String accessToken = generateToken(username,roles);
        String refreshToken = generateRefreshToken(username);
        return new JoinResponseDTO(accessToken, refreshToken);
    }

    public RefreshTokenService getRefreshTokenService() {
        return refreshTokenService;
    }
}
