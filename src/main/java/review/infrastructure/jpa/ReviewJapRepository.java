package review.infrastructure.jpa;


import java.util.UUID;

public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderHistoryId(UUID orderHistoryId);

    Page<Review> findAllByStoreId(UUID storeId, Pageable pageable);

    Page<Review> findAllByUserId(Long userId, Pageable pageable);
}
