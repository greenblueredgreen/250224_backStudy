package review.application.dto.response;

import review.domain.repository.vo.Review;
import java.time.LocalDateTime;
import java.util.UUID;

//record는 불변객체이다
//한번 생성되면 못바꾼다.
//record사용시 자동으로 getter, equals, hashCode 생성
//단, setter는 안된다. record(불변객체)이기 때문에 값 변경 불간
public record ReviewResponse(
        UUID reviewId,
        UUID orderHistoryId,
        Long userId,
        String nickname,
        String content,
        Integer rating,
        LocalDateTime reviewTime) {

    //정적 팩토리 메소드
    //of() ReviewReponse 객체로 반환해주는 역할
    public static ReviewResponse of(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderHistory().getId(),
                review.getUser().getId(),
                review.getUser().getNickname().getValue(),
                review.getContent().getValue(),
                review.getRating().getValue(),
                review.getReviewTime().getValue()
        );
    }
}