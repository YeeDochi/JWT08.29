package com.example.prectice2.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.example.prectice2.Service.AuthService;
import com.example.prectice2.User.UserEntity;
import com.example.prectice2.User.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
// 인증, 인가 관련 컨트롤러 
// 뭔가 많이 지저분해짐 고치고싶음
public class AuthController {
   
    private final UserRepository userRepository;
    private final AuthService authService;

    // 권한 확인 엔드포인트
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(user);
    }

    // 리프레시 엔드포인트
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            authService.handleReissue(request, response, authentication);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
