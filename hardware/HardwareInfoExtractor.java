import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// 이 클레스는 리눅스에서 사용하기위한 라이선스 유효성 검증 클레스 입니다.

public class HardwareInfoExtractor {

    private String SECRET_KEY = "asdfqwefadsvfjJHJAJBUI123NBDIKA1"; // 이건 나중에 환경변수같은 곳으로 빼기

    private int GCM_IV_LENGTH = 12;
    private int GCM_TAG_LENGTH = 128;

    // public HardwareInfoExtractor() {
    // }

    public HardwareInfoExtractor(String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public HardwareInfoExtractor(String secretKey, int iv){
        this.SECRET_KEY = secretKey;
        this.GCM_IV_LENGTH = iv;
    }

    public boolean validateHardwareWithLicense(Map<String, String> licenseMap, Map<String, String> hwMap) {
        String[] keys = {"coreCount", "socketCount", "boardSerial", "macAddress", "expireDate"};
        String expireDateKey = "expireDate";
        for (String key : keys) {
            if (key.equals(expireDateKey)) continue; // 만료일은 마지막에 따로 판단
            String licVal = licenseMap.getOrDefault(key, "N/A");
            String hwVal = hwMap.getOrDefault(key, "N/A");
            if (!licVal.equals("N/A") && !hwVal.equals("N/A")) {
                // 숫자 비교가 가능한 필드(coreCount, socketCount)
                if (key.equals("coreCount") || key.equals("socketCount")) {
                    try {
                        int licNum = Integer.parseInt(licVal);
                        int hwNum = Integer.parseInt(hwVal);
                        if (hwNum > licNum) return false; // 실제 값이 라이선스 값 초과면 false
                    } catch (Exception e) { return false; }
                } else {
                    // 문자열(시리얼, MAC)은 정확히 일치
                    if (!licVal.equals(hwVal)) return false;
                }
            }
        }
        // 만료일만 다를 경우 만료일 체크
        String licExpire = licenseMap.getOrDefault(expireDateKey, "N/A");
        if (!licExpire.equals("N/A")) {
            java.time.LocalDate now = java.time.LocalDate.now();
            try {
                java.time.LocalDate expire = java.time.LocalDate.parse(licExpire);
                return now.isBefore(expire);
            } catch (Exception e) {
                return false; // 만료일 파싱 실패 시 false
            }
        }
        return true;
    }

    public Map<String, String> parseLicenseSpecs(String raw) {  //라이선스 raw 맵핑
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isEmpty()) return result;
        String[] parts = raw.split("/");
        String typeStr = parts.length > 0 ? parts[0] : "N/A";
        int typeInt = 0;
        try { typeInt = Integer.parseInt(typeStr); } catch (Exception e) {}
        String[] keys = {"coreCount", "socketCount", "boardSerial", "macAddress", "expireDate"};
        int[] flags = {1,2,4,8,16};
        int idx = 1;
        for (int i = 0; i < keys.length; i++) {
            if ((typeInt & flags[i]) != 0 && parts.length > idx) {
                result.put(keys[i], parts[idx]);
                idx++;
            }
        }
        return result;
    }


    private String executeCommand(String command) { // 커멘드 실행
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder("bash", "-c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Command failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public Map<String, String> getHardwareSpecs(String raw) { // 하드웨어 파싱 and 멥핑
        Map<String, String> specs = new HashMap<>();

        if (raw == null || raw.isEmpty()) return specs;
        String[] parts = raw.split("/");
        String typeStr = parts.length > 0 ? parts[0] : "N/A";
        int typeInt = 0;
        try { typeInt = Integer.parseInt(typeStr); } catch (Exception e) {}

        if ((typeInt & 1) != 0) {
            String cpuOutput = executeCommand("lscpu | grep -E 'Core\\(s\\) per socket|Socket\\(s\\)'");
        specs.put("coreCount", parseCpuInfo(cpuOutput, "Core(s) per socket:"));
        }
        if ((typeInt & 2) != 0) {
            String cpuOutput = executeCommand("lscpu | grep -E 'Core\\(s\\) per socket|Socket\\(s\\)'");
            specs.put("socketCount", parseCpuInfo(cpuOutput, "Socket(s):"));
        }
        if ((typeInt & 4) != 0) {
             String boardOutput = executeCommand("sudo dmidecode -t 1 | grep 'Serial Number:'");
        specs.put("boardSerial", parseDmiInfo(boardOutput, "Serial Number:"));
        }
        if ((typeInt & 8) != 0) {
            String nicOutput = executeCommand("ip link | grep 'link/ether' | awk '{print $2}'");
            specs.put("macAddress", nicOutput.trim().replace("\n", ", "));
        }
        if ((typeInt & 16) != 0) {

        }
        return specs;
    }

    private String parseCpuInfo(String output, String key) { // CPU 정보 파싱
        for (String line : output.split("\n")) {
            if (line.contains(key)) {
                return line.split(":")[1].trim();
            }
        }
        return "N/A";
    }

    private String parseDmiInfo(String output, String key) { // DMI 정보 파싱
        if (output.contains(key)) {
            return output.split(":")[1].trim();
        }
        return "N/A";
    }

    public String validateLicense(String licenseCode) {
        try {
            byte[] decoded = Base64.getDecoder().decode(licenseCode);
            // iv와 암호화된 데이터를 분리
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv); // 벡터
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData); // 데이터

            // 키값과 벡터로 복호화
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

    public boolean LicenseCheck(String licenseCode) {
        String raw = validateLicense(licenseCode);
        Map<String, String> specs = getHardwareSpecs(raw);
        Map<String, String> parsedSpecs = parseLicenseSpecs(raw);
        return validateHardwareWithLicense(parsedSpecs, specs);
    }

    public static void main(String[] args) {
        HardwareInfoExtractor extractor = new HardwareInfoExtractor();

        String arg0 = args.length > 0 ? args[0] : null;
        String arg1 = args.length > 1 ? args[1] : null;

        if (arg1 != null && arg1.equals("a")) {
            String raw = arg0 != null ? extractor.validateLicense(arg0) : null;
            Map<String, String> specs = extractor.getHardwareSpecs(raw);
            Map<String, String> parsedSpecs = extractor.parseLicenseSpecs(raw);
            System.out.println("Hardware Specifications:");
            specs.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println("Parsed License Information:");
            parsedSpecs.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println("Hardware Validation Result: " + (extractor.validateHardwareWithLicense(parsedSpecs, specs) ? "Valid" : "Invalid"));
        } else if (arg0 != null) {
            System.out.println("Hardware Validation Result: " + (extractor.LicenseCheck(arg0) ? "Valid" : "Invalid"));
        } else {
            System.out.println("라이선스 코드를 인자로 입력하세요.");
        }

    }
}