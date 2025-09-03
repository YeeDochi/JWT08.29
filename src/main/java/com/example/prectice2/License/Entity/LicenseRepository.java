package com.example.prectice2.License.Entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.prectice2.License.DTO.LicenseDTO;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface LicenseRepository extends JpaRepository<LicenseEntity, Long> {
    @Query("SELECT new com.example.prectice2.License.DTO.LicenseDTO(l.coreCount, l.socketCount, l.boardSerial, l.macAddress, l.expireDate, l.type) FROM LicenseEntity l WHERE l.id = :id")
    Optional<LicenseDTO> findDtoById(@Param("id") Long id);

}
