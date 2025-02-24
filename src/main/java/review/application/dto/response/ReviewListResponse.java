package review.application.dto.response;

import org.springframework.data.domain.Page;
import review.domain.repository.vo.Review;

import java.util.List;

//record 는 불변 객체를 만들 때 사용한다.
//한번 생성되면 상태가 변하지 않는 객체로, 객체 생성 시 모든 값이 설정되며, 이후 변경 불가능
public record ReviewListResponse(List<ReviewResponse> reviewResponseList, int totalPages,
                                 long totalElements) {

    public static ReviewListResponse of(Page<Review> reviews) {
        return new ReviewListResponse(
                reviews.stream().map(ReviewResponse::of).toList(),
                reviews.getTotalPages(),
                reviews.getTotalElements()
        );
    }
}