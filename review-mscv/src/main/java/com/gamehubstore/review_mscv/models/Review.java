package com.gamehubstore.review_mscv.models;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import java.time.LocalDateTime;
@Entity @Table(name="reviews",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","order_id","product_id"}))
public class Review { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotNull @Column(name="user_id",nullable=false) private Long userId; @NotNull @Column(name="product_id",nullable=false) private Long productId;
 @NotNull @Column(name="order_id",nullable=false) private Long orderId; @NotNull @Min(1) @Max(5) @Column(nullable=false) private Integer rating;
 @NotBlank @Size(max=2000) @Column(nullable=false,length=2000) private String comment; @Column(nullable=false) private boolean active=true;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;} public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;} public Integer getRating(){return rating;} public void setRating(Integer v){rating=v;} public String getComment(){return comment;} public void setComment(String v){comment=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
