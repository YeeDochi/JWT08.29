import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;



public class KeyGenerater {
    // 이 코드는 딱 한 번만 실행해서 키 문자열을 얻는 용도입니다.
    public static void generateAndPrintKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        System.out.println("--- 영구적으로 사용할 개인키 (서버에 안전하게 보관) ---");
        System.out.println(privateKey);

        System.out.println("\n--- 영구적으로 사용할 공개키 (제품에 탑재) ---");
        System.out.println(publicKey);
    }

    public static void main(String[] args) {
        try {
            generateAndPrintKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
