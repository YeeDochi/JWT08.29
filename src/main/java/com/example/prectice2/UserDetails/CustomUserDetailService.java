package com.example.prectice2.UserDetails;

import com.example.prectice2.User.UserEntity;
import com.example.prectice2.User.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { 
        // db에서 데이터를 뽑아 userdetail 객체를 생성
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        if (userEntity.isPresent()) {
            UserEntity user = userEntity.get();
            return new CustomUserDetails(user);
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
