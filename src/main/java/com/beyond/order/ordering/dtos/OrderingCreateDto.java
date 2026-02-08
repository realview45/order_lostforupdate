package com.beyond.order.ordering.dtos;

import com.beyond.order.member.domain.Member;
import com.beyond.order.ordering.domain.OrderStatus;
import com.beyond.order.ordering.domain.Ordering;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.time.LocalDateTime.now;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingCreateDto {
    @NotBlank
    private Long productId;
    @NotBlank
    private Long productCount;
    public static Ordering toEntity(Member member) {
        return Ordering.builder()
                .orderStatus(OrderStatus.ordered)
                .member(member)
                .build();
    }
}
