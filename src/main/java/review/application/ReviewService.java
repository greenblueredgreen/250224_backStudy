package review.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import review.domain.repository.ReviewRepository;
import review.domain.repository.vo.Review;

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
    //새로운 리뷰 생성, 가게 평점 업데이트
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

    //리뷰 수정 service, 가게 평점 update
    @Transactional //DB변경이 발생하므로 어노테이션 적용
    public ReviewResponse updateReview(final UUID reviewId, final ReviewUpdateRequest request,
                                       final UserDetailsImpl userDetails) {
        
        //수정할리뷰 조회
        final Review review = getReviewOrElseThrow(reviewId); //reviewId에 해당하는거 없으면 예외 발생

        //현재 유저가 리뷰 작성자인지 확인
        validateUser(review.getUser().getId(), userDetails.getUserId());

        //기존 평점 저장 후 리뷰 수정 -> 새 리뷰 정보로 업데이트
        final BigDecimal oldRating = review.getRating().getBigDecimalValue();
        review.update(request.content(), request.rating(), request.reviewTime());
        final BigDecimal newRating = review.getRating().getBigDecimalValue();

        //가게 평점 다시 계산 - 0 : 리뷰 개수는 그대로, oldRating 이전 평점, newRation 수정된 평점
        storeService.calculateRating(review.getStoreId(), 0, oldRating, newRating);

        //수정된 리뷰 반환
        return ReviewResponse.of(review);
    }

    //리뷰 삭제 (데이터 완전 삭제 안하고 삭제된 상태로 변경)
    @Transactional
    public void softDeleteReview(final UUID reviewId, final UserDetailsImpl userDetails) {

        //현재 유저 정보 조회 -> 로그인 한 유저 정보 가져와서 권한 확인
        final User user = userService.getUserOrElseThrow(userDetails.getUserId());
        final UserRoleEnum userRole = user.getRole();

        //삭제할 리뷰 조회, 삭제 리뷰 없으면 예외 발생
        final Review review = getReviewOrElseThrow(reviewId);
        
        //리뷰 삭제
        review.softDelete();

        //가게 평점 다시 계산 : -1 : 리뷰 감소, 
        storeService.calculateRating(review.getStoreId(), -1,
                review.getRating().getBigDecimalValue(), // 삭제된 리뷰의 기존 평점
                BigDecimal.ZERO); //삭제된 평점 처리
    }

    //리뷰 조회 기능
    //특정 리뷰(reviewId)로 조회하는 기능, 없으면 예외처리
    public ReviewResponse getReviewInfo(final UUID reviewId) {
        
        //찾으면 리뷰 객체를 ReviewResponse 형태로 변환 후 반환
        return ReviewResponse.of(getReviewOrElseThrow(reviewId));
    }
    
    //위의 함수 - 115번째 
    //리뷰 없으면 예외 처리 발생시키는 함수
    public Review getReviewOrElseThrow(final UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    //특정 가게의 리뷰 목록을 페이징 처리
    public ReviewListResponse getReviewsByStoreId(final UUID storeId, final int page,
                                                  final int size,
                                                  final String sortBy, final boolean isAsc) {

        storeService.getStoreOrElseThrow(storeId);
        
        //맨 아래 createPageable 함수
        final Pageable pageable = createPageable(page, size, sortBy, isAsc);
        
        //DB에서 storeId에 해당하는 리뷰들 들고오기
        Page<Review> reviews = reviewRepository.findAllByStoreId(storeId, pageable);

        //review를 ReviewListResponse로 변환(DTO로 변환하는 메소드 of())
        //DTO로 변환해 내려주기
        return ReviewListResponse.of(reviews);
    }

    //특정 유저가 작성한 리뷰 목록을 페이징 처리해 조회
    public ReviewListResponse getReviewsByUserId(final Long userId, final int page, final int size,
                                                 final String sortBy, final boolean isAsc) {

        //userId에 해당하는 유저가 존재하는지 확인, 없으면 예외 처리
        userService.getUserOrElseThrow(userId);
        
        //아래에서 만든 createPageable 호출 - 같은 클래스 내부라 private도 호출 가능
        final Pageable pageable = createPageable(page, size, sortBy, isAsc);

        //DB에서 해당하는 유저의 리뷰들 들고오기
        Page<Review> reviews = reviewRepository.findAllByUserId(userId, pageable);

        //조회된 리뷰 목록을 DTO로 변환
        //Review entity 그대로 반환시 DB구조에 대한 정보 노출 가능.
        //DTO를 사용해 필요 데이터만 전달!
        //of()가reviews 데이터를 ReviewListResponse 객체로 변환하는 역할
        //생성자를 감추고 정적메서도 of로 객체 생성
        return ReviewListResponse.of(reviews);
    }

    //Spring Data JPA의 페이징(Pageable) 기능을 생성하는 역할
    //Pageable 객체를 생성하는 역할
    //이 객체는 페이징 및 정렬 정보를 포함한 요청 객체
    //page : 페이지 번호, size : 페이지 한 개당 몇개 데이터 들고올지, sortBy, 정렬 기준, isAsc true 오름차순
    //private는 같은 클래스 내부에서만 호출 가능
    private Pageable createPageable(final int page, final int size,
                                    final String sortBy, final boolean isAsc) {

        //isAsc가 true면 ASC, false면 DESC
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        //정렬 기준을 설정, sortBy를 기준으로 정렬, direction은 내림, 오름차순 결정
        Sort sort = Sort.by(direction, sortBy);

        //PageRequest.of(page, size, sort)를 사용해 페이징 요청 객체(Pageable)를 생성
        return PageRequest.of(page, size, sort);
        //정렬방향 결정, 정렬 기준 필드 설정, 페이징 객체 생성
    }
}
