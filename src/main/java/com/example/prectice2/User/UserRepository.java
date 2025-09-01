package com.example.prectice2.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 사용되는 쿼리(?)들

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
