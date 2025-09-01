package com.example.prectice2.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.prectice2.Filters.JwtAuthenticationFilter;
import com.example.prectice2.Filters.LoginFilters;
import com.example.prectice2.JWT.JwtUtil;
import com.example.prectice2.UserDetails.CustomUserDetailService;

import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
        public SecurityFilterChain securityWebFilterChain(HttpSecurity http) throws Exception {
                http // 기본 필터 설정 (JWT용)
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .csrf(AbstractHttpConfigurer::disable)
                        .formLogin(AbstractHttpConfigurer::disable)
                        .httpBasic(AbstractHttpConfigurer::disable)
                        .logout(AbstractHttpConfigurer::disable)
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers("/api/admin/join","/api/member/join","/api/reissue").permitAll()
                                        .requestMatchers("/member/**","/api/me","/logout").authenticated()
                                        .requestMatchers("/admin/**").hasRole("ADMIN")
                                        .anyRequest().permitAll()
                        );
                http // 추가한 필터 설정 (1회용 인증 필터(authenticated 설정일시 사용됨)-> 로그인 필터)
                        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, customUserDetailService), UsernamePasswordAuthenticationFilter.class)
                        .addFilterAt(new LoginFilters(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        // CORS 설정
        @Bean
        public CorsConfigurationSource corsConfigurationSource() { 
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.addAllowedOriginPattern("*");
                configuration.addAllowedMethod("*");
                configuration.addAllowedHeader("*");
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
