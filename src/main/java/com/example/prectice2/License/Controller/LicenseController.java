package com.example.prectice2.License.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.prectice2.License.DTO.LicenseDTO;
import com.example.prectice2.License.DTO.LicenseData;
import com.example.prectice2.License.Entity.LicenseEntity;
import com.example.prectice2.License.Entity.LicenseRepository;
import com.example.prectice2.License.Service.FormattedLicenseService;
import io.swagger.v3.oas.models.info.License;
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

    private final FormattedLicenseService formattedLicenseService;
    private final LicenseRepository licenseRepository;
    // 라이선스 데이터 저장
    @PostMapping
    public ResponseEntity<Long> createLicense(@RequestBody LicenseDTO dto) {
        LicenseEntity entity = new LicenseEntity();
        entity.setType(dto.type());
        entity.setCoreCount(dto.coreCount());
        entity.setSocketCount(dto.socketCount());
        entity.setBoardSerial(dto.boardSerial());
        entity.setMacAddress(dto.macAddress());
        entity.setExpireDate(dto.expireDate());
        // 필요하면 추가 필드도 설정

        LicenseEntity saved = licenseRepository.save(entity);
        return ResponseEntity.ok(saved.getId());
    }

    // 라이선스 키 반환
    @GetMapping("/{id}")
    public ResponseEntity<String> getLicenseKey(@PathVariable Long id) throws Exception {
        //String licenseKey = licenseService.getLicenseKeyById(id);
        String formatted = formattedLicenseService.createLicenseKey(licenseRepository.findDtoById(id).orElse(null));
        return ResponseEntity.ok(formatted);
    }

   
    @GetMapping("/admin/decode")
    public ResponseEntity<LicenseDTO> decodeLicense(@RequestParam String licenseKey) throws Exception {
        LicenseDTO decoded = formattedLicenseService.decodeLicenseKey(licenseKey);
        return ResponseEntity.ok(decoded);
    }

}
