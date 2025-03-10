package review.presentation;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import review.application.ReviewService;
import review.application.dto.request.ReviewCreateRequest;
import review.application.dto.request.ReviewUpdateRequest;
import review.application.dto.response.ReviewListResponse;
import review.application.dto.response.ReviewResponse;
import review.enums.UserRoleEnum;


//각 어노테이션 어떤 것을 하는지, 흐름을 어떻게 타고 가느지, git 한 것처럼 하나하나 자세히 분석해보기. 생성자 초기화 다 보기.
//왜 이 어노테이션을 사용했느지...
//금요일 2시 전에 방문하기
//get, post 파라미터
//소나큐브 설치


@RestController  //rest api 요청을 처리 컨트롤러
@RequiredArgsConstructor  //final로 선언된 필드(ReviewService)를 자동으로 생성자 주입
@RequestMapping("/reviews")
public class ReviewController {

    //ReviewService를 주입받아서 service계층 로직 호출 역할
    private final ReviewService reviewService;

    //new 생성자 안해도 되고....초기화 부분..
    //자세히 하나하나 분석

    //리뷰 생성 POST
    //@Secured : Spring Security에서 특정권한을 가진 사용자만 해당 메서드에 접근하도록 제한
    @Secured({UserRoleEnum.Authority.CUSTOMER, UserRoleEnum.Authority.MANAGER, UserRoleEnum.Authority.MASTER})
    @PostMapping("/{orderHistoryId}")
    public ResponseEntity<CommonResponse> createReview(
            //현재 로그인한 사용자 정보를 가져옴
            //로그인 된 사용자 정보로 리뷰를 작성하는 것이다.
            //즉, 로그인 한 본인의 리뷰를 쓰는 것이기 때문에 userId가 아닌 UserDetailsImpl을 들고오는 것이다.
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            //URL에서 orderHistoryId 값을 받아옴 => PathVariable
            @PathVariable UUID orderHistoryId,
            //요청 바디(JSON)에서 ReviewCreateRequest 데이터를 가져와 유효성 검증.
            @Valid @RequestBody ReviewCreateRequest request) {
        
        //서비스 계층에서 review 생성 로직 실행
        ReviewResponse response = reviewService.createReview(orderHistoryId, request, userDetails);
        //응답 데이터를 성공으로 변환 -> CREATED 상태 코드와 함께 반환
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(response),
                HttpStatus.CREATED);
    }
    
    //리뷰 수정 PUT
    @Secured({UserRoleEnum.Authority.CUSTOMER, UserRoleEnum.Authority.MANAGER, UserRoleEnum.Authority.MASTER})
    @PutMapping("/{reviewId}")
    public ResponseEntity<CommonResponse> updateReview(
            //현재 로그인한 사용자 정보를 가져옴
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        
        //service에서 리뷰 업데이트
        ReviewResponse response = reviewService.updateReview(reviewId, request, userDetails);
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(response),
                HttpStatus.OK);
    }

    //리뷰 삭제 - DELETE
    //CUSTOMER는 삭제할 수 없고 MANAGER, MASTER만 삭제 가능
    //그 이유는, 배민의 경우 시간이 지나면 리뷰를 가게주인, 손님 모두 사용할 수 없게 해놓았다.
    //그래서 삭제를 본인이 못하게 막아놓은 것이다.
    @Secured({UserRoleEnum.Authority.MANAGER, UserRoleEnum.Authority.MASTER})
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<CommonResponse> softDeleteReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID reviewId) {
        
        //service 에서 소프트 삭제 - 데이터를 삭제하는게 아니라 비활성화
        reviewService.softDeleteReview(reviewId, userDetails);
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(),
                HttpStatus.OK);
    }

    //리뷰 상세 조회 - GET
    @GetMapping("/{reviewId}")
    public ResponseEntity<CommonResponse> getReviewInfo(
            @PathVariable UUID reviewId) {

        //service에서 review데이터 가져온다.
        ReviewResponse response = reviewService.getReviewInfo(reviewId);
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(response),
                HttpStatus.OK);
    }

    //가게별 리뷰 조회 (페이징 포함) - GET
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse> getReviewsByStoreId(
            @PathVariable UUID storeId,
            @RequestParam int page,  //몇 번째 페이지인지(0부터 시작)
            @RequestParam int size, //한 페이지에 몇 개의 데이터
            @RequestParam String sortBy, //정렬 기준(날짜, 평점...)
            //오름차순 정렬
            @RequestParam boolean isAsc ) {
        
        // service에서 가계리뷰리스트 들고오기
        ReviewListResponse response = reviewService.getReviewsByStoreId(storeId, page, size, sortBy,
                isAsc);
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(response),
                HttpStatus.OK);
    }

    //사용자별 리뷰 조회(페이징 포함) - GET
    //여기서는 userId인 이유는 다른 사람의 리뷰도 볼수있게 하기 위해서이다.
    //다른 사람의 리뷰는 누구나 볼 수 있기 때문에 위에서 만든 로그인된 사용자가 아닌 userId로 한것이다.
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse> getReviewsByUserId(
            @PathVariable Long userId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortBy,
            @RequestParam boolean isAsc) {

        //service에서 사용자별 리스트 들고옴.
        ReviewListResponse response = reviewService.getReviewsByUserId(userId, page, size, sortBy,
                isAsc);
        return new ResponseEntity<>(ResponseVOUtils.getSuccessResponse(response),
                HttpStatus.OK);
    }
}
