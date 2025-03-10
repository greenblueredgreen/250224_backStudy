package review.infrastructure.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import review.domain.repository.vo.Review;

import java.util.UUID;

//JpaRepository<Review, UUID>를 상속받았기 때문에
// 기본적인 CRUD 기능 (save, findById, deleteById 등) 제공됨
// Spring Data JPA 가 자동으로 구현



//실제 jpa 실행
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {

    //save와 findById 빼고 다 구현
    boolean existsByOrderHistoryId(UUID orderHistoryId);

    Page<Review> findAllByStoreId(UUID storeId, Pageable pageable);

    Page<Review> findAllByUserId(Long userId, Pageable pageable);
}