package review.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;


//record는 불변타입
public record ReviewUpdateRequest(String content,
                                  @NotBlank(message = "평점은 필수입니다.") Integer rating,
                                  LocalDateTime reviewTime) {

}