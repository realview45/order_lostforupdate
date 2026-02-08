package com.beyond.order.ordering.dtos;
import com.beyond.order.ordering.domain.OrderStatus;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.domain.OrderingDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingListDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    @Builder.Default
    private List<OrderingDetailsListDto> orderingDetailsListDtoList=new ArrayList<>();

    public static OrderingListDto fromEntity(Ordering ordering){
        List<OrderingDetailsListDto> orderDetailDtos =new ArrayList<>();
        for(OrderingDetails od : ordering.getOrderList()){
            orderDetailDtos.add(OrderingDetailsListDto.fromEntity(od));
        }
        OrderingListDto orderingListDto = OrderingListDto.builder()
                .id(ordering.getId())
                .orderStatus(ordering.getOrderStatus())
                .memberEmail(ordering.getMember().getEmail())
                .orderingDetailsListDtoList(orderDetailDtos).build();

        return orderingListDto;
    }
}
//OrderDetail->OrderDetailDto로 변환 메서드(OrderDetailDto에서)
//fromEntity List<OrderDetailDto> Deatails조립 먼저하고 빌더로 리턴