package review.domain.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import review.domain.repository.vo.Review;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    //JPA가 Review entity 자동으로 저장 메소드
    Review save(Review review);

    //동일한 주문에 대해 리뷰가 이미 등록된 경우 예외 발생 service의 JPA
    //존재시 true, 없으면 false
    //리뷰 중복 등록 방지
    boolean existsByOrderHistoryId(UUID orderHistoryId);

    //reviewId로 특정 리뷰 조회
    //리뷰 없을 수 있기 때문에 Optional 사용
    //SELECT * FROM review WHERE id = ?;
    Optional<Review> findById(UUID reviewId);

    //특정 가게 리뷰 페이징 처리 service 
    //storeId에 대한 리뷰리스트 조회
    //Page<Review> 를 반환 → 페이징 처리된 결과를 반환
    //Pageable 파라미터를 받음 → 페이징 & 정렬을 적용할 수 있음
    Page<Review> findAllByStoreId(UUID storeId, Pageable pageable);

    //특정 유저가 작성한 리뷰 목록 조회
    //Page<Review> 를 반환 → 페이징 & 정렬 적용 가능
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
}
