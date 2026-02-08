package com.beyond.order.product.domain;

import com.beyond.order.common.domain.BaseTimeEntity;
import com.beyond.order.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable=false)
    private String name;
    @Column(nullable=false)
    private Long price;
    private String category;
    @Column(nullable=false)
    private Long stockQuantity;
    private String imagePath;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String imgUrl) {
        imagePath = imgUrl;
    }
    public void updateStockQuantity(Long orderQuantity) {
        this.stockQuantity = this.stockQuantity-orderQuantity;
    }
}
