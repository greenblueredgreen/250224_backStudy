package review.enums;

import lombok.Getter;

import java.io.Serializable;

@Getter 
//ENUM : 열거형, 상수들의 집합을 나타내는 타입
//역할을 정의한 열거형이다
public enum UserRoleEnum implements Serializable {
    //ENUM의 상수들
    CUSTOMER(Authority.CUSTOMER), OWNER(Authority.OWNER), MANAGER(Authority.MANAGER), MASTER(
            Authority.MASTER);

    private final String authority; //자동 GETTER함수 생성

    //각 ENUM 상수에 권한 값을 설정하기 위해 사용
    //ENUM 선언시 함께 호출
    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    //상수들을 관리하는 역할
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
