package com.example.prectice2.License.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.example.prectice2.License.DTO.LicenseDTO;


public interface LicenseFieldHandler {
    // 직렬화: DTO에서 값을 읽어 스트림에 쓴다
    void serialize(DataOutputStream dos, LicenseDTO dto) throws IOException;
    // 역직렬화: 스트림에서 값을 읽어 Builder의 상태를 변경한다
    void deserialize(DataInputStream dis, LicenseDTO.Builder builder) throws IOException;

}
