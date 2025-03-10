package review.exception;

public class ReviewForbiddenException extends CustomForbiddenException {

    public ReviewForbiddenException() {
        super("해당 리뷰에 권한이 없습니다.");
    }
}

