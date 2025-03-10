package review.exception;

import java.util.UUID;

public class ReviewNotFoundException extends CustomNotFoundException {

    public ReviewNotFoundException(final UUID id) {
        super(String.format(
                "조회한 리뷰가 존재하지 않습니다. - 요청 정보 { reviewId : %s }",
                id));
    }
}
