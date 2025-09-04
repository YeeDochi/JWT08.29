package com.example.prectice2.License.Service;

import lombok.RequiredArgsConstructor;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

// String 으로 저장되어있는 키를 객체로 변환하는 클래스

@Component
@RequiredArgsConstructor
public class KeyLoader {

    @Value("${license.private-key}")
    private String PRIVATE_KEY_STRING;

    @Value("${license.public-key}")
    private String PUBLIC_KEY_STRING;

    public PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyPem = PRIVATE_KEY_STRING;
        if (privateKeyPem == null) {
            throw new RuntimeException("입력된 개인키가 없습니다.");
        }
        privateKeyPem = privateKeyPem.replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);


        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyPem = PUBLIC_KEY_STRING; 

        publicKeyPem = publicKeyPem.replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);


        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}