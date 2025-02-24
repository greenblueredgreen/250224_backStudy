package review.domain.repository.vo;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_review")
@Entity
public class Review extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_history_id", nullable = false)
    private OrderHistory orderHistory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "store_id")
    private UUID storeId;

    @Embedded
    private ReviewContent content;

    @Embedded
    private Rating rating;

    @Embedded
    private ReviewTime reviewTime;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    public Review(final OrderHistory orderHistory, final User user, final String content,
                  final Integer rating,
                  final LocalDateTime reviewTime) {
        this.orderHistory = orderHistory;
        this.user = user;
        this.storeId = orderHistory.getStoreId();
        this.content = new ReviewContent(content);
        this.rating = new Rating(rating);
        this.reviewTime = new ReviewTime(reviewTime, orderHistory.getCompletionTime());
        this.isDeleted = false;
    }

    public void update(final String content, final Integer rating, final LocalDateTime reviewTime) {
        this.content = new ReviewContent(content);
        this.rating = new Rating(rating);
        this.reviewTime = new ReviewTime(reviewTime, orderHistory.getCompletionTime());
    }

    public void softDelete() {
        this.isDeleted = true;
    }

}