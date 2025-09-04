package com.example.prectice2.License.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.example.prectice2.License.DTO.LicenseDTO;
import com.example.prectice2.License.DTO.LicenseDTO.Builder;

public class CoreCountHandler implements LicenseFieldHandler {
    private static final int BITMASK = 1;

    @Override
    public void serialize(DataOutputStream dos, LicenseDTO dto) throws IOException {
         if ((dto.getType() & BITMASK) != 0 && dto.getCoreCount() != null) {
            dos.writeInt(dto.getCoreCount());
        }
    }

    @Override
    public void deserialize(DataInputStream dis, Builder builder) throws IOException {
        if ((builder.build().getType() & BITMASK) != 0) {
            builder.coreCount(dis.readInt());
        }
    }
}
