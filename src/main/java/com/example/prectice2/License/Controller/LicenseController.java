package com.example.prectice2.License.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.prectice2.License.DTO.LicenseDTO;
import com.example.prectice2.License.Service.LicenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    // 라이선스 데이터 저장
    @PostMapping
    public ResponseEntity<Long> createLicense(@RequestBody LicenseDTO dto) {
        Long id = licenseService.saveLicense(dto);
        return ResponseEntity.ok(id);
    }

    // 라이선스 키 반환
    @GetMapping("/{id}")
    public ResponseEntity<String> getLicenseKey(@PathVariable Long id) {
        String licenseKey = licenseService.getLicenseKeyById(id);
        return ResponseEntity.ok(licenseKey);
    }

   
    @GetMapping("/admin/decode")
    public ResponseEntity<String> decodeLicense(@RequestParam String licenseKey) {
        String decoded = licenseService.validateLicense(licenseKey);
        return ResponseEntity.ok(decoded);
    }

}
