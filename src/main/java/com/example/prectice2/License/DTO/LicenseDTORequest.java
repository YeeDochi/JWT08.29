package com.example.prectice2.License.DTO;


public record LicenseDTORequest (
    Integer coreCount,
    Integer socketCount,
    String boardSerial,
    String macAddress,
    String expireDate,
    Integer type
) {}

/*
 * DTO 가 변경된다면 Data 와 Entity, Repository 도 같이 변경되어야 함
 */