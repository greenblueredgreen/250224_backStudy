package review.exception;

import java.util.UUID;

public class ReviewAlreadyExistsException extends CustomBadRequestException {

    public ReviewAlreadyExistsException(final UUID orderId) {
        super(String.format(
                "해당 주문에 작성한 리뷰가 있습니다. - 요청 정보 { orderId : %s }",
                orderId));
    }
}
