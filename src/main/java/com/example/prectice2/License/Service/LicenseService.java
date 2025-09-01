package com.example.prectice2.License.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;


import com.example.prectice2.License.Entity.LicenseEntity;
import com.example.prectice2.License.Entity.LicenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private static final String SECRET_KEY = "asdfqwefadsvfjJHJAJBUI123NBDIKA1"; //이것도 후에 프로퍼티스로 밀어넣기

    private final LicenseRepository licenseRepository;
    // GCM IV(초기화 벡터) 길이
    private static final int GCM_IV_LENGTH = 12;
    // GCM 인증 태그 길이
    private static final int GCM_TAG_LENGTH = 128;

    public static String createLicense(LicenseEntity LE) {
        StringBuilder rawBuilder = new StringBuilder();
        try {
            // type을 제외한 5가지 필드 비트 플래그 매핑
            // 1: coreCount, 2: socketCount, 4: boardSerial, 8: macAddress, 16: expireDate
            int typeInt;
            try {
                typeInt = Integer.parseInt(LE.getType());
            } catch (NumberFormatException e) {
                typeInt = 0; // type이 숫자가 아니면 아무 필드도 포함하지 않음
            }
            rawBuilder.append(LE.getType());
            if ((typeInt & 1) != 0) rawBuilder.append(":" + LE.getCoreCount());
            if ((typeInt & 2) != 0) rawBuilder.append(":" + LE.getSocketCount());
            if ((typeInt & 4) != 0) rawBuilder.append(":" + LE.getBoardSerial());
            if ((typeInt & 8) != 0) rawBuilder.append(":" + LE.getMacAddress());
            if ((typeInt & 16) != 0) rawBuilder.append(":" + LE.getExpireDate());
            String raw = rawBuilder.toString();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // 안전한 IV 생성

            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] encryptedData = cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("라이선스 코드 생성 중 오류 발생", e);
        }
    }

    public static String validateLicense(String licenseCode) {
        try {
            byte[] decoded = Base64.getDecoder().decode(licenseCode);
            
            // IV와 암호화된 데이터를 분리합니다.
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (javax.crypto.AEADBadTagException e) {
            throw new RuntimeException("라이선스가 유효하지 않거나 변조되었습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("라이선스 코드 유효성 검사 중 오류 발생", e);
        }
    }

    // public String encoder(LicenseEntity LE) {

    //     try {
    //         String raw = LE.getCoreCount() + ":" + LE.getSocketCount() + ":" + LE.getBoardSerial() + ":" + LE.getMacAddress() + ":" + LE.getExpireDate() + ":" + LE.getType();
    //         MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //         byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
          
    //         String base32 = new Base32().encodeToString(hash).replace("=", "");
           
    //         String serial = base32.substring(0, 16).toUpperCase();
    //         String formatted = serial.replaceAll("(.{4})", "$1-").replaceAll("-$", "");
    //         return formatted;
    //     } catch (java.security.NoSuchAlgorithmException e) {
    //         throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
    //     }
    // }

    public String decoder(String licenseKey) {
        return null;
    }

    public Long saveLicense(com.example.prectice2.License.DTO.LicenseDTO dto) {
          LicenseEntity entity = new LicenseEntity(
                dto.coreCount(),
                dto.socketCount(),
                dto.boardSerial(),
                dto.macAddress(),
                dto.expireDate(),
                dto.type()
          );
          LicenseEntity savedEntity = licenseRepository.save(entity);
          return savedEntity.getId();
     }

     public String getLicenseKeyById(Long id) {
         LicenseEntity entity = licenseRepository.findById(id).orElse(null);
         return entity != null ? createLicense(entity) : null;
     }
}
