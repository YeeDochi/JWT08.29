package com.example.prectice2.License.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;


import com.example.prectice2.License.Entity.LicenseEntity;
import com.example.prectice2.License.Entity.LicenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private static final String SECRET_KEY = "YourSuperSecretLicenseKey1234567"; 
    
    // GCM 모드에서 권장하는 IV(초기화 벡터) 길이 (12바이트)
    private static final int GCM_IV_LENGTH = 12;

    // GCM 인증 태그 길이 (128비트 = 16바이트)
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * 라이선스 정보를 암호화하여 라이선스 코드를 생성합니다.
     */
    public static String createLicense(LicenseEntity LE) {
        String raw = null;
        try {
            switch(LE.getType()){
                case "TRIAL":
                    raw = LE.getType()+":"+LE.getExpireDate();
                    break;
                case "1":
                    raw = LE.getType()+":"+LE.getCoreCount();
                    break;
                case "2":
                    raw = LE.getType()+":"+LE.getBoardSerial();
                    break;
                case "4":
                    raw = LE.getType()+":"+LE.getSocketCount();
                    break;
                case "8":
                    raw = LE.getType()+":"+LE.getMacAddress();
                    break;
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // 안전한 IV 생성

            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] encryptedData = cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            
            // IV와 암호화된 데이터를 결합하여 하나의 바이트 배열로 만듭니다.
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
            
            // 복호화 과정에서 태그(HMAC 역할)가 검증됩니다. 
            // 위변조된 코드라면 AEADBadTagException이 발생합니다.
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (javax.crypto.AEADBadTagException e) {
            throw new RuntimeException("라이선스가 유효하지 않거나 변조되었습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("라이선스 코드 유효성 검사 중 오류 발생", e);
        }
    }


    private final LicenseRepository licenseRepository;

    public String encoder(LicenseEntity LE) {

        try {
            String raw = LE.getCoreCount() + ":" + LE.getSocketCount() + ":" + LE.getBoardSerial() + ":" + LE.getMacAddress() + ":" + LE.getExpireDate() + ":" + LE.getType();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
          
            String base32 = new Base32().encodeToString(hash).replace("=", "");
           
            String serial = base32.substring(0, 16).toUpperCase();
            String formatted = serial.replaceAll("(.{4})", "$1-").replaceAll("-$", "");
            return formatted;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

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
