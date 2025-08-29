package com.example.prectice2.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.prectice2.DTO.JoinRequestDTO;
import com.example.prectice2.User.UserEntity;
import com.example.prectice2.User.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String join(JoinRequestDTO joinRequestDTO, String role) {
            // username 중복 체크
            if (userRepository.findByUsername(joinRequestDTO.username()).isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 username입니다.");
            }
            String encodedPassword = passwordEncoder.encode(joinRequestDTO.password());
            userRepository.save(new UserEntity(joinRequestDTO.username(), encodedPassword, joinRequestDTO.email(), role));
            return "회원가입 완료";
    }
}
