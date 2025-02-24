package review.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ReviewCreateRequest(
        String content,
        @NotBlank(message = "평점은 필수입니다.") Integer rating,
        LocalDateTime reviewTime) {

}