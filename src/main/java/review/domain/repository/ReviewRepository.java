package review.domain.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import review.domain.repository.vo.Review;

import java.util.Optional;
import java.util.UUID;

//JpaRepository를 상속받지 않음 → JPA에 직접 의존하지 않도록 설계됨
//jpa 대신 mybatis 같은 다른 DB기술로 변경시 ReviewRepositoryImpl구현체만 바꾸면됨
//이 인터페이스는 구현체(ReviewRepositoryImpl)에서 직접 구현해야 함
//독립적인 인터페이스 유지

//여기는 껍데기이다.
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
    //Optional 타입 : 컨테이너 클래스
    //값이 존재할 수도, 존재하지 않을 수도 있는 경우 사용
    // null 값으로 인한 NullPointerException 방지
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
