package com.example.prectice2.License.DTO;


public record LicenseDTO (
    String coreCount,
    String socketCount,
    String boardSerial,
    String macAddress,
    String expireDate,
    String type
) {}
