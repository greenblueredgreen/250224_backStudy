package review.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


@Service  //비지니스 로직 담당
@RequiredArgsConstructor //final 필드 자동으로 주입하는 lombok 어노테이션
@Transactional(readOnly = true) //읽기 전용 트랜잭션 적용 성능 최적화
public class ReviewService {

    private final UserService userService;
    private final OrderHistoryService orderHistoryService;
    private final StoreService storeService;
    private final ReviewRepository reviewRepository;

    //리뷰생성 service
    @Transactional //DB변경이 발생하므로 어노테이션 적용
    public ReviewResponse createReview(final UUID orderHistoryId, final ReviewCreateRequest request,
                                       final UserDetailsImpl userDetails) {

        //현재 로그인한 userId를 가져와사 db에서 정보를 찾음.
        final Long userId = userDetails.getUserId();
        final User user = userService.getUserOrElseThrow(userId); //없으면 예외 발생

        //orderHistoryId에 해당하는 주문 내역을 가져옴, 주문 내역이 없다면 예외 발생
        final OrderHistory orderHistory = orderHistoryService.getOrderHistoryOrElseThrow(orderHistoryId);

        validateUser(orderHistory.getUserId(), userId); 

        checkIfReviewExists(orderHistoryId);

        //새로운 리뷰 객체 생성, save를 통해 저장
        final Review review = new Review(orderHistory, user, request.content(), request.rating(), request.reviewTime());
        final Review saved = reviewRepository.save(review);

        //가게 평점 계산 업데이트
        storeService.calculateRating(saved.getStoreId(),
                1, // 리뷰 개수를 증가
                BigDecimal.ZERO,  //기존 리뷰 평점 (없으므로 0)
                review.getRating().getBigDecimalValue()); // 새로 작성된 평점

        return ReviewResponse.of(saved); //저장된 리뷰 정보 반환
    }
    
    //37번째 줄
    //주문한 사람이 맞는지 확인, 아니면 예외 발생
    private void validateUser(Long requestId, Long expectedId) {
        if (!requestId.equals(expectedId)) {
            throw new ReviewForbiddenException();
        }
    }

    //동일한 주문에 대해 리뷰가 이미 등록된 경우 예외 발생
    private void checkIfReviewExists(final UUID orderHistoryId) {
        if (reviewRepository.existsByOrderHistoryId(orderHistoryId)) {
            throw new ReviewAlreadyExistsException(orderHistoryId);
        }
    }

    //리뷰 수정 service
    @Transactional //DB변경이 발생하므로 어노테이션 적용
    public ReviewResponse updateReview(final UUID reviewId, final ReviewUpdateRequest request,
                                       final UserDetailsImpl userDetails) {
        
        //수정할리뷰 조회
        final Review review = getReviewOrElseThrow(reviewId);
        validateUser(review.getUser().getId(), userDetails.getUserId());

        //기존 평점 저장 후 리뷰 수정
        final BigDecimal oldRating = review.getRating().getBigDecimalValue();
        review.update(request.content(), request.rating(), request.reviewTime());
        final BigDecimal newRating = review.getRating().getBigDecimalValue();

        storeService.calculateRating(review.getStoreId(), 0, oldRating, newRating);
        return ReviewResponse.of(review);
    }

    @Transactional
    public void softDeleteReview(final UUID reviewId, final UserDetailsImpl userDetails) {
        final User user = userService.getUserOrElseThrow(userDetails.getUserId());
        final UserRoleEnum userRole = user.getRole();

        final Review review = getReviewOrElseThrow(reviewId);
        review.softDelete();

        storeService.calculateRating(review.getStoreId(), -1,
                review.getRating().getBigDecimalValue(),
                BigDecimal.ZERO);
    }

    public ReviewResponse getReviewInfo(final UUID reviewId) {
        return ReviewResponse.of(getReviewOrElseThrow(reviewId));
    }

    public Review getReviewOrElseThrow(final UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    public ReviewListResponse getReviewsByStoreId(final UUID storeId, final int page,
                                                  final int size,
                                                  final String sortBy, final boolean isAsc) {
        storeService.getStoreOrElseThrow(storeId);
        final Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<Review> reviews = reviewRepository.findAllByStoreId(storeId, pageable);
        return ReviewListResponse.of(reviews);
    }

    public ReviewListResponse getReviewsByUserId(final Long userId, final int page, final int size,
                                                 final String sortBy, final boolean isAsc) {
        userService.getUserOrElseThrow(userId);
        final Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<Review> reviews = reviewRepository.findAllByUserId(userId, pageable);
        return ReviewListResponse.of(reviews);
    }

    private Pageable createPageable(final int page, final int size,
                                    final String sortBy, final boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

}
