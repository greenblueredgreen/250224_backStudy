package review.domain.repository.vo;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter

//lombok 어노테이션, 파라미터가 없는 생성자를 자동으로 생성
// 접근제어 수준을 protected로 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)

//JPA어노테이션으로 테이블 매핑
@Table(name = "p_review")

//JPA 어노테이션으로 entity 클래스를 나타냄
//이 클래스는 반드시 DB table과 일치해야함
@Entity
public class Review extends TimeStamp {

    //entity 기본키
    //entity 클래스는 id로 정의된 기본키를 가져야한다.
    //JPA 어노테이션, id값을 UUID로 생성 지정
    //UUID는 중복가능성이 없는 고유식별자
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID id;


    //JPA 어노테이션으로 다대일 관계를 설정
    //OrderHistory와 연결, OrderHistory에 여러 개의 Review 있음
    //fetch = FetchType.LAZY는 orderHistory를 지연 로딩 방식으로 가져오도록 설정
    //optional = false은 Review가 반드시 OrderHistroy를 가져와야한다는것을 의미
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //JPA 어노테이션으로 외래 키 관계를 설정
    //Review entity는 OrderHistory entity의 order_history_id를 외래키로 사용
    @JoinColumn(name = "order_history_id", nullable = false)
    private OrderHistory orderHistory;

    //다대일 관계
    //User와 연결, 한 User는 여러 개의 리뷰 작성 가능
    //option = false는 Review가 반드시 User와 연결되어야함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "store_id")
    private UUID storeId;

    //JPA어노테이션, 임베디드 타입
    //JPA에서 복합적인 값을 저장하고자 할 때 쓰는 타입
    //하나의 객체로 여러개의 속성을 그룹화 시킴
    //복잡한 데이터를 하나의 필드처럼 다루고 싶을 때.
    @Embedded
    private ReviewContent content;

    @Embedded
    private Rating rating;

    @Embedded
    private ReviewTime reviewTime;

    //entity 필드와 DB 테이블 컬럼을 매핑하는역할
    @Column(name = "is_deleted") //DB테이블 연결
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
        this.isDeleted = false; //리뷰 삭제 되지 않은 상태로 설정
    }

    //기존 리뷰 수정할 때 사용
    public void update(final String content, final Integer rating, final LocalDateTime reviewTime) {
        this.content = new ReviewContent(content);
        this.rating = new Rating(rating);
        this.reviewTime = new ReviewTime(reviewTime, orderHistory.getCompletionTime());
    }

    //리뷰가 삭제된 상태로 표시
    public void softDelete() {
        this.isDeleted = true;
    }
}