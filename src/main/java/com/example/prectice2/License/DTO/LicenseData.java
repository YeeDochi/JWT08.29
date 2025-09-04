package com.example.prectice2.License.DTO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.prectice2.License.Handler.CoreCountHandler;
import com.example.prectice2.License.Handler.LicenseFieldHandler;


// DTO의 데이터를 고정 길이 바이트 배열로 변환하는 클래스
public class LicenseData {
    
    // 기준 날짜 (Epoch) - 이 날짜로부터 며칠이 지났는지를 저장 (좀더 효율적) 
    private static final LocalDate EPOCH_DATE = LocalDate.of(2020, 1, 1);

    

     private static final List<LicenseFieldHandler> HANDLERS = List.of(
        // 새로운 필드 핸들러를 여기에 추가!
        new CoreCountHandler()
    );

    public static byte[] toByteArray(LicenseDTO dto) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(dto.getType()); // 타입은 먼저 쓴다.
        for (LicenseFieldHandler handler : HANDLERS) {
            handler.serialize(dos, dto);
        }
        return baos.toByteArray();
    }


    // 바이트 배열로부터 LicenseData 객체 생성 (역직렬화)
    public static LicenseDTO fromByteArray(byte[] bytes) throws java.io.IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int typeInt = dis.readInt();
        LicenseDTO.Builder builder = LicenseDTO.builder().type(typeInt);
        for (LicenseFieldHandler handler : HANDLERS) {
            handler.deserialize(dis, builder);
        }

        // //임시 변수
        // Integer coreCount = (typeInt & 1) != 0 ? dis.readInt() : null;
        // Integer socketCount = (typeInt & 2) != 0 ? dis.readInt() : null;
        // String boardSerial = (typeInt & 4) != 0 ? readString(dis) : null;
        // String macAddress = (typeInt & 8) != 0 ? readString(dis) : null;
        // Integer tempDays = (typeInt & 16) != 0 ? dis.readInt() : null;
        // String expireDate = tempDays != null ? EPOCH_DATE.plusDays(tempDays).toString() : null;

        /* 
            추후 필드 추가 가능
            예: String newField = (typeInt & 32) != 0 ? readString(dis) : null;
            예: Integer anotherField = (typeInt & 64) != 0 ? dis.readInt() : null;
            등등
            */

        return builder.build();
    }

   // [길이][데이터] 쓰기
    private void writeString(DataOutputStream dos, String str) throws  java.io.IOException {
        if (str == null) {
            dos.writeShort(0);
            return;
        }
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        dos.writeShort(strBytes.length); 
        dos.write(strBytes);
    }

    // [길이][데이터] 읽기
    private static String readString(DataInputStream dis) throws java.io.IOException {
        int length = dis.readShort();
        if (length == 0) return null;
        byte[] strBytes = new byte[length];
        dis.readFully(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }
}