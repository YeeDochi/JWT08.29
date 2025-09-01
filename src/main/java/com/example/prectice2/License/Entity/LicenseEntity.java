package com.example.prectice2.License.Entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

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

    public LicenseEntity(String coreCount, String socketCount, String boardSerial, String macAddress, String expireDate, String type) {
        this.coreCount = coreCount;
        this.socketCount = socketCount;
        this.boardSerial = boardSerial;
        this.macAddress = macAddress;
        this.expireDate = expireDate;
        this.type = type;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String coreCount;
    private String socketCount;
    private String boardSerial;
    private String macAddress;
    private String expireDate;
    private String type;

      @CreationTimestamp
    private Timestamp createDate;

}

