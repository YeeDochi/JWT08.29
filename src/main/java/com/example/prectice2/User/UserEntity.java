package com.example.prectice2.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 사용자 정보를 담는 엔티티
// 뭐가 많다야..
@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class UserEntity {

    public UserEntity(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String role;

    @CreationTimestamp
    private Timestamp createDate;


    public List<String> getRoleList() {
        if(!this.role.isEmpty()) {
            return Arrays.asList(this.role.split(","));
        }
        return new ArrayList<>();
    }


}
