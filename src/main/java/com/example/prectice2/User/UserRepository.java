package com.example.prectice2.User;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	// username(혹은 email)으로 사용자 조회
	Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
