package review.enums;

import lombok.Getter;

import java.io.Serializable;

@Getter
public enum UserRoleEnum implements Serializable {
    CUSTOMER(Authority.CUSTOMER), OWNER(Authority.OWNER), MANAGER(Authority.MANAGER), MASTER(
            Authority.MASTER);

    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public static class Authority {

        public static final String CUSTOMER = "CUSTOMER";
        public static final String OWNER = "OWNER";
        public static final String MANAGER = "MANAGER";
        public static final String MASTER = "MASTER";

        private Authority() {
            throw new UnsupportedOperationException(
                    "유틸리티 클래스는 인스턴스를 바깥에서 생성할 수 없음");
        }
    }
}
