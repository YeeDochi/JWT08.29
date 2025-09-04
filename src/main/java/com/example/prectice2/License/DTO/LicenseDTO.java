package com.example.prectice2.License.DTO;

public class LicenseDTO {
    private final Integer coreCount;
    private final Integer socketCount;
    private final String boardSerial;
    private final String macAddress;
    private final String expireDate;
    private final Integer type;

    // 생성자는 Builder를 통해서만 호출되도록 private으로 설정합니다.
    private LicenseDTO(Builder builder) {
        this.coreCount = builder.coreCount;
        this.socketCount = builder.socketCount;
        this.boardSerial = builder.boardSerial;
        this.macAddress = builder.macAddress;
        this.expireDate = builder.expireDate;
        this.type = builder.type;
    }

    // 외부에서 Builder 객체를 생성할 수 있도록 정적 메서드를 제공합니다.
    public static Builder builder() {
        return new Builder();
    }

    // 각 필드에 대한 Getter 메서드
    public Integer getCoreCount() { return coreCount; }
    public Integer getSocketCount() { return socketCount; }
    public String getBoardSerial() { return boardSerial; }
    public String getMacAddress() { return macAddress; }
    public String getExpireDate() { return expireDate; }
    public Integer getType() { return type; }
    
    // toString() 메서드를 오버라이드하여 디버깅 시 편의성을 높입니다.
    @Override
    public String toString() {
        return "LicenseDTO{" +
                "coreCount=" + coreCount +
                ", socketCount=" + socketCount +
                ", boardSerial='" + boardSerial + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", expireDate='" + expireDate + '\'' +
                ", type=" + type +
                '}';
    }


    // --- 정적 내부 클래스로 Builder 구현 ---
    public static class Builder {
        private Integer coreCount;
        private Integer socketCount;
        private String boardSerial;
        private String macAddress;
        private String expireDate;
        private Integer type;

        // 각 필드를 설정하는 메서드는 Builder 객체 자신을 반환하여 메서드 체이닝(chaining)이 가능하게 합니다.
        public Builder coreCount(Integer coreCount) {
            this.coreCount = coreCount;
            return this;
        }

        public Builder socketCount(Integer socketCount) {
            this.socketCount = socketCount;
            return this;
        }

        public Builder boardSerial(String boardSerial) {
            this.boardSerial = boardSerial;
            return this;
        }

        public Builder macAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder expireDate(String expireDate) {
            this.expireDate = expireDate;
            return this;
        }

        public Builder type(Integer type) {
            this.type = type;
            return this;
        }

        // build() 메서드를 통해 최종적으로 불변하는 LicenseDTO 객체를 생성합니다.
        public LicenseDTO build() {
            return new LicenseDTO(this);
        }
    }
}
