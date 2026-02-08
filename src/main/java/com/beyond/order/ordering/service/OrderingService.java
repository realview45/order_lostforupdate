package com.beyond.order.ordering.service;

import com.beyond.order.common.service.SseAlarmService;
import com.beyond.order.member.domain.Member;
import com.beyond.order.member.repository.MemberRepository;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.repository.OrderingDetailsRepository;
import com.beyond.order.ordering.repository.OrderingRepository;
import com.beyond.order.ordering.domain.OrderingDetails;
import com.beyond.order.ordering.dtos.OrderingDetailsListDto;
import com.beyond.order.product.domain.Product;
import com.beyond.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailsRepository orderingDetailsRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final SseAlarmService sseAlarmService;
    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderingDetailsRepository orderingDetailsRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailsRepository = orderingDetailsRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
    }

    public Long create(List<OrderingCreateDto> dtoList) {
        //email을 인증객체에서 꺼내서 오더링 빌더로 만들고, 저장,
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        Ordering ordering = OrderingCreateDto.toEntity(member);
        List<OrderingDetails> orderList = ordering.getOrderList();
        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티가없습니다."));
            if(product.getStockQuantity()<dto.getProductCount()){//요청전부다 취소될것임구리
                throw new IllegalArgumentException("재고가 없습니다.");
            }
            System.out.println("상품ID: " + dto.getProductId());
            System.out.println("수량: " + dto.getProductCount());
            product.updateStockQuantity(dto.getProductCount());
            OrderingDetails od =
                    OrderingDetails.builder()
                            .product(productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티를 찾을 수 없습니다.")))
                            .quantity(dto.getProductCount())
                            .ordering(ordering).build();
            orderList.add(od);//cascade persist
        }
        orderingRepository.save(ordering);
//        주문성공시 admin 유저에게 알림메시지 전송
        String message = ordering.getId()+ "번 주문이 발생했습니다.";
        sseAlarmService.sendMessage("admin@naver.com",email, message);
        return ordering.getId();
    }

    public List<OrderingListDto> findAll() {
        List<Ordering> orderingList = orderingRepository.findAll();
        List<OrderingListDto> dtoList = new ArrayList<>();
        for(Ordering o : orderingList){
            OrderingListDto orderingListDto = OrderingListDto.fromEntity(o);
            dtoList.add(orderingListDto);
        }
        return dtoList;
        //return orderingRepository.findAll().stream().map(o-> OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }
    public List<OrderingListDto> myorders(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        return orderingRepository.findAllByMember(member).stream().map(o-> OrderingListDto.fromEntity(o)).collect(Collectors.toList());

//        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("엔티티가 없습니다."));
//        List<Ordering> orderingList = member.getOrderingList();
//        List<OrderingListDto> dtoList = new ArrayList<>();
//        for(Ordering o : orderingList){
//            List<OrderingDetails> detailsList = o.getOrderList();
//            List<OrderingDetailsListDto> detailsListDtoList = detailsList.stream().map(d-> OrderingDetailsListDto.fromEntity(d)).collect(Collectors.toList());
//            OrderingListDto orderingListDto = OrderingListDto.builder()
//                    .id(o.getId())
//                    .memberEmail(o.getMember().getEmail())
//                    .orderStatus(o.getOrderStatus())
//                    .orderingDetailsListDtoList(detailsListDtoList).build();
//            dtoList.add(orderingListDto);
//        }
//        return dtoList;
    }
}
