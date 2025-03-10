package review.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

//record 는 객체의 불변성을 보장
public record ReviewCreateRequest(
        String content,   //리뷰 내용
        //NotBlank 어노테이션
        // => 유효성 검사를 위한 것, 평점은 반드시 값을 가져야한다는 것을 의미합니다
        // 유효성 검사는 사용자가 입력한 데이터가 형식이나 조건에 맞는지 확인하는 과정
        @NotBlank(message = "평점은 필수입니다.") Integer rating,
        
        //리뷰가 작성된 시간을 저장
        LocalDateTime reviewTime) {
}