package com.example.prectice2.License.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

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

    private static final int DEFAULT_CHUNK_SIZE = 8; // 8글자씩 끊어서 포멧팅
    private static final String SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding"; // AES/GCM 대칭키 알고리즘
    private static final String ASYMMETRIC_SIGNATURE_ALGORITHM = "SHA256withRSA"; // RSA 서명 알고리즘

    // 대칭키 방식때 사용되는 변수들
    @Value("${license.SECRET_KEY}")
    private String SECRET_KEY;
    @Value("${license.GCM_IV_LENGTH}")
    private int GCM_IV_LENGTH;
    @Value("${license.GCM_TAG_LENGTH}")
    private int GCM_TAG_LENGTH;

    private final KeyLoader keyLoader;

    public String createLicenseKey(LicenseDTO dto) throws Exception { // 대칭키 방식
        System.out.println("CreateKey DTO val: " + dto);
        // DTO를 압축된 바이트 배열로 변환
        LicenseData data = new LicenseData();
        byte[] rawData = data.toByteArray(dto);

        // AES/GCM 암호화 
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encryptedData = cipher.doFinal(rawData);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        
        String base32Encoded = new Base32().encodeToString(byteBuffer.array());
        String replacedPadding = base32Encoded.replace('=', '0');
        return formatBase32ToKey(replacedPadding, DEFAULT_CHUNK_SIZE); // 8개씩 포멧팅
   }


   public String createLicenseKey(LicenseDTO dto, int temp) throws Exception { // 비대칭키 방식
        System.out.println("CreateKey DTO val: " + dto+", temp: "+temp);
        // DTO를 압축된 바이트 배열로 변환
        LicenseData data = new LicenseData();
        byte[] rawData = data.toByteArray(dto);
        int dataLength = rawData.length;
        PrivateKey privateKey = keyLoader.loadPrivateKey();
        // ECDSA 서명 생성
        Signature ecdsaSign = Signature.getInstance(ASYMMETRIC_SIGNATURE_ALGORITHM);
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(rawData);
        byte[] signature = ecdsaSign.sign(); 

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + dataLength + signature.length);
        byteBuffer.putShort((short) dataLength);
        byteBuffer.put(rawData);
        byteBuffer.put(signature);
        byte[] finalBytes = byteBuffer.array();

        String base32Encoded = new Base32().encodeToString(finalBytes);
        String replacedPadding = base32Encoded.replace('=', '0');
        return formatBase32ToKey(replacedPadding, DEFAULT_CHUNK_SIZE); // 8개씩 포멧팅
   }


    public LicenseDTO decodeLicenseKey(String formattedKey) throws Exception { // 대칭키 디코딩
        // 포멧 풀기
        String base32Encoded = formattedKey.replace("-", "").toUpperCase().replaceAll("=", "");
        String restoredBase32 = base32Encoded.replace('0', '=');
        byte[] finalBytes = new Base32().decode(restoredBase32);
        // IV와 암호문 분리
        ByteBuffer byteBuffer = ByteBuffer.wrap(finalBytes);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedData = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedData);

        // AES/GCM 복호화
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);
       
        return LicenseData.fromByteArray(decryptedData);
    }

     public LicenseDTO decodeLicenseKey(String formattedKey,int temp) throws Exception { // 대칭키 디코딩
        // 포멧 풀기
        String base32Encoded = formattedKey.replace("-", "").toUpperCase().replaceAll("=", "");
        String restoredBase32 = base32Encoded.replace('0', '=');
        byte[] finalBytes = new Base32().decode(restoredBase32);
        ByteBuffer byteBuffer = ByteBuffer.wrap(finalBytes);

        short dataLength = byteBuffer.getShort(); // 맨 앞 2바이트를 읽어 데이터 길이 확인
        byte[] rawData = new byte[dataLength];
        byteBuffer.get(rawData); // 길이만큼 읽어서 원본 데이터 추출
        byte[] signature = new byte[byteBuffer.remaining()];
        byteBuffer.get(signature); // 나머지 전부를 서명으로 추출

        PublicKey publicKey = keyLoader.loadPublicKey(); 

        Signature ecdsaVerify = Signature.getInstance(ASYMMETRIC_SIGNATURE_ALGORITHM);
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(rawData); // 원본 데이터를 넣어서
        boolean isValid = ecdsaVerify.verify(signature); // 서명이 맞는지 확인
       
        if (isValid) {
            System.out.println("유효한 라이선스입니다.");
            // 원본 데이터를 DTO로 변환하여 반환
            return LicenseData.fromByteArray(rawData);
        } else {
            System.out.println("위조된 라이선스입니다!");
            throw new SecurityException("Invalid license key signature.");
        }
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
