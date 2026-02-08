package com.beyond.order.ordering.domain;

import com.beyond.order.common.domain.BaseTimeEntity;
import com.beyond.order.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import static java.time.LocalDateTime.now;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class OrderingDetails extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private Ordering ordering;
    @ManyToOne
    private Product product;
}
