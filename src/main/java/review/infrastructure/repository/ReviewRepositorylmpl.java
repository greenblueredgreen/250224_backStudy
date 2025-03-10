package review.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import review.domain.repository.ReviewRepository;
import review.domain.repository.vo.Review;
import review.infrastructure.jpa.ReviewJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
//ReviewRepository를 구현한 클래스
//현재는 단순히 JPA 기능을 감싸는 역할만 하지만,
// 추가적인 비즈니스 로직을 넣을 수도 있음

//implemets를 했기 때문에, 반드시 인터페이스 메소드를 구현해야한다.
//여기서 껍데기를 받아와 구현을 해준다.
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;

    @Override
    public Review save(Review review) {
        return jpaRepository.save(review);
    }

    @Override
    public boolean existsByOrderHistoryId(UUID orderHistoryId) {
        return jpaRepository.existsByOrderHistoryId(orderHistoryId);
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return jpaRepository.findById(reviewId);
    }

    @Override
    public Page<Review> findAllByStoreId(UUID storeId, Pageable pageable) {
        return jpaRepository.findAllByStoreId(storeId, pageable);
    }

    @Override
    public Page<Review> findAllByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findAllByUserId(userId, pageable);
    }

}