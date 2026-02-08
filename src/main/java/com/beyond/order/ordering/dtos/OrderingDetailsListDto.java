package com.beyond.order.ordering.dtos;

import com.beyond.order.ordering.domain.OrderingDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingDetailsListDto {
    private Long detailId;
    private String productName;
    private Long productCount;
    public static OrderingDetailsListDto fromEntity(OrderingDetails orderingDetails){
        return OrderingDetailsListDto.builder().detailId(orderingDetails.getId())
                .productName(orderingDetails.getProduct().getName())
                .productCount(orderingDetails
                        .getQuantity()).build();
    }
}
