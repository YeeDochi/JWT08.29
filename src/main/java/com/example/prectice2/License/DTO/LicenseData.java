package com.example.prectice2.License.DTO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


// DTO의 데이터를 고정 길이 바이트 배열로 변환하는 클래스
public class LicenseData {

    // 기준 날짜 (Epoch) - 이 날짜로부터 며칠이 지났는지를 저장
    private static final LocalDate EPOCH_DATE = LocalDate.of(2020, 1, 1);

    private final LicenseDTO dto;

    public LicenseData(LicenseDTO dto) {
        this.dto = dto;
    }

     public byte[] toByteArray() throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int typeInt = 0;
        try {
            typeInt = Integer.parseInt(dto.type());
        } catch (Exception ignored) {}
        dos.writeInt(typeInt);

        if ((typeInt & 1) != 0) dos.writeShort(parseShortSafely(dto.coreCount()));
        if ((typeInt & 2) != 0) dos.writeShort(parseShortSafely(dto.socketCount()));
        if ((typeInt & 4) != 0) writeString(dos, dto.boardSerial());
        if ((typeInt & 8) != 0) writeString(dos, dto.macAddress());
        if ((typeInt & 16) != 0) {
            try {
                LocalDate expireDate = LocalDate.parse(dto.expireDate());
                long days = ChronoUnit.DAYS.between(EPOCH_DATE, expireDate);
                dos.writeInt((int) days);
            } catch (Exception e) {
                dos.writeInt(0); // 파싱 실패 시 기본값 0
            }
        }
        return baos.toByteArray();
    }

    private short parseShortSafely(String s) {
        try {
            return Short.parseShort(s);
        } catch (NumberFormatException e) {
            return 0; // 파싱 실패 시 기본값 0
        }
    }


    // 바이트 배열로부터 LicenseData 객체 생성 (역직렬화)
    public static LicenseDTO fromByteArray(byte[] bytes) throws java.io.IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int typeInt = dis.readInt();
        
        //임시 변수
        Short tempCoreCount = (typeInt & 1) != 0 ? dis.readShort() : null;
        Short tempSocketCount = (typeInt & 2) != 0 ? dis.readShort() : null;
        String tempBoardSerial = (typeInt & 4) != 0 ? readString(dis) : null;
        String tempMacAddress = (typeInt & 8) != 0 ? readString(dis) : null;
        Integer tempDays = (typeInt & 16) != 0 ? dis.readInt() : null;

        //최종 변수 -> DTO 에 들어갈 변수
        String coreCount = tempCoreCount != null ? String.valueOf(tempCoreCount) : null;
        String socketCount = tempSocketCount != null ? String.valueOf(tempSocketCount) : null;
        String boardSerial = tempBoardSerial;
        String macAddress = tempMacAddress;
        String expireDate = tempDays != null ? EPOCH_DATE.plusDays(tempDays).toString() : null;
    
        return new LicenseDTO(coreCount, socketCount, boardSerial, macAddress, expireDate, String.valueOf(typeInt));
    }

   // Helper: [길이][데이터] 쓰기
    private void writeString(DataOutputStream dos, String str) throws  java.io.IOException {
        if (str == null) {
            dos.writeShort(0);
            return;
        }
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        dos.writeShort(strBytes.length); // 길이를 2바이트 short으로 저장
        dos.write(strBytes);
    }

    // Helper: [길이][데이터] 읽기
    private static String readString(DataInputStream dis) throws java.io.IOException {
        int length = dis.readShort();
        if (length == 0) return null;
        byte[] strBytes = new byte[length];
        dis.readFully(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }
}