package com.example.prectice2.License.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.prectice2.License.DTO.LicenseDTO;
import com.example.prectice2.License.DTO.LicenseData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormattedLicenseService {
    @Value("${license.SECRET_KEY}")
    private String SECRET_KEY;
    @Value("${license.GCM_IV_LENGTH}")
    private int GCM_IV_LENGTH;
    @Value("${license.GCM_TAG_LENGTH}")
    private int GCM_TAG_LENGTH;

   
    public String createLicenseKey(LicenseDTO dto) throws Exception {
        System.out.println("CreateKey DTO val: " + dto);
        // DTO를 압축된 바이트 배열로 변환
        LicenseData data = new LicenseData(dto);
        byte[] rawData = data.toByteArray();

        // AES/GCM 암호화 (이전과 동일)
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encryptedData = cipher.doFinal(rawData);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        
        String base32Encoded = new Base32().encodeToString(byteBuffer.array());
        
        // --- 개선점 1 적용: 포매팅 메소드 호출 ---
        return formatBase32ToKey(base32Encoded, 8);
   }

    public LicenseDTO decodeLicenseKey(String formattedKey) throws Exception {
       // --- 개선점 1 적용: 포맷 제거 ---
       String base32Encoded = formattedKey.replace("-", "").toUpperCase().replaceAll("=", "");
       
       byte[] finalBytes = new Base32().decode(base32Encoded);
        // IV와 암호문 분리 (이전과 동일)
        ByteBuffer byteBuffer = ByteBuffer.wrap(finalBytes);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedData = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedData);

        // AES/GCM 복호화 (이전과 동일)
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);
       
        return LicenseData.fromByteArray(decryptedData);
    }

    private String formatBase32ToKey(String str, int chunkSize) { // 특정 자리수만큼씩 자르기
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < str.length(); i += chunkSize) {
            if (i > 0) {
                formatted.append("-");
            }
            formatted.append(str.substring(i, Math.min(i + chunkSize, str.length())));
        }
        System.out.println(formatted.toString());
        return formatted.toString();
    }
}
