package com.example.prectice2.License.Entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.example.prectice2.License.DTO.LicenseDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "licenses")
@NoArgsConstructor
@Getter
public class LicenseEntity {

    public LicenseEntity(int coreCount, int socketCount, String boardSerial, String macAddress, String expireDate, int type) {
        this.coreCount = coreCount;
        this.socketCount = socketCount;
        this.boardSerial = boardSerial;
        this.macAddress = macAddress;
        this.expireDate = expireDate;
        this.type = type;
    }

    public LicenseEntity(LicenseDTO dto) {
        this.coreCount = dto.getCoreCount();
        this.socketCount = dto.getSocketCount();
        this.boardSerial = dto.getBoardSerial();
        this.macAddress = dto.getMacAddress();
        this.expireDate = dto.getExpireDate();
        this.type = dto.getType();
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer coreCount;
    private Integer socketCount;
    private String boardSerial;
    private String macAddress;
    private String expireDate;
    private Integer type;

      @CreationTimestamp
    private Timestamp createDate;

    public LicenseDTO toDTO() {
    return LicenseDTO.builder()
            .coreCount(this.coreCount)
            .socketCount(this.socketCount)
            .boardSerial(this.boardSerial)
            .macAddress(this.macAddress)
            .expireDate(this.expireDate)
            .type(this.type)
            .build();
}
}

