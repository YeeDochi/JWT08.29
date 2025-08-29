package com.example.prectice2.JWT;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 저장
    public void setRefreshToken(String username, String refreshToken) { // 로그인 시에만 사용.
        redisTemplate.opsForValue().set(username, refreshToken);
    }

    // 조회
    public String getRefreshToken(String username) { // 엑세스 토큰 발급시 사용.
        return redisTemplate.opsForValue().get(username);
    }

    // 삭제
    public void deleteRefreshToken(String username) { // 로그아웃, 만료, 토큰 발급 후에 사용.
        redisTemplate.delete(username);
    }
}
