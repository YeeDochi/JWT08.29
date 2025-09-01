package com.example.prectice2.UserDetails;

import com.example.prectice2.User.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

// 유저 디테일 객체, 시큐리티에서 기본적으로 사용하는 유저정보를 개조 유저 토큰 인증에 사용
// 가지고 있는 유저정보로 토큰을 만들어 받은 토큰과 비교하는듯

@Getter
public class CustomUserDetails implements UserDetails {

    private UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoleList().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public String getUserEmail(){
        return user.getEmail();
    }

    public String getUserRole(){
        return user.getRoleList().get(0);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
