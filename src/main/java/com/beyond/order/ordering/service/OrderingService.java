package com.beyond.order.ordering.service;

import com.beyond.order.common.service.SseAlarmService;
import com.beyond.order.member.domain.Member;
import com.beyond.order.member.repository.MemberRepository;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.domain.OrderingDetails;
import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.repository.OrderingDetailsRepository;
import com.beyond.order.ordering.repository.OrderingRepository;
import com.beyond.order.product.domain.Product;
import com.beyond.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
    private final RedisTemplate<String, String> redisTemplate;
    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderingDetailsRepository orderingDetailsRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailsRepository = orderingDetailsRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }
    //동시성이슈 해결방안1.
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create(List<OrderingCreateDto> dtoList) {
        //email을 인증객체에서 꺼내서 오더링 빌더로 만들고, 저장,
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        Ordering ordering = OrderingCreateDto.toEntity(member);
        List<OrderingDetails> orderList = ordering.getOrderList();
        orderingRepository.save(ordering);//레디스쪽에서 ordering객체가 save되지않은 상태에서
        for (OrderingCreateDto dto : dtoList) {
            //동시성제어방법2. select for update통한 락설정이후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티가없습니다."));
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티가없습니다."));

            //          동시성제어방법3. redis에서 재고수량 확인 및 재고수량 감소처리
//            String remain = redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
//            int remainQuantity = Integer.parseInt(remain);
//            if(remainQuantity < dto.getProductCount()){
//                throw new IllegalArgumentException("재고가 부족합니다");
//            }else {
//                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()), dto.getProductCount());
//            }

            String key = String.valueOf(dto.getProductId());//keys[1]
            String amount = String.valueOf(dto.getProductCount());//argv[1]

            // Lua 스크립트 정의 및 실행
            String script = "local stock = redis.call('get', KEYS[1]) " +
                    "if stock == false then return -2 end " +
                    "if tonumber(stock) < tonumber(ARGV[1]) then return -1 " +
                    "else return redis.call('decrby', KEYS[1], ARGV[1]) end";

            // execute(스크립트객체, 키리스트, 인자리스트)
            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(key),
                    amount
            );

            if (result == -1) {
                throw new IllegalArgumentException("재고가 부족합니다. 상품 ID: " + key);
            } else if (result == -2) {
                throw new EntityNotFoundException("Redis에 해당 상품 재고 정보가 없습니다. 상품 ID: " + key);
            }



            //            if(product.getStockQuantity()<dto.getProductCount()){//요청전부다 취소될것임구리
//                throw new IllegalArgumentException("재고가 없습니다.");
//            }
            System.out.println("상품ID: " + dto.getProductId());
            System.out.println("수량: " + dto.getProductCount());
            //200명이 조회해서 같은값을 읽어서 insert 재고빼버릴때 동시성이슈 발생
            //이중 튕겨나가는 주문을 데드락이라고 함
//            product.updateStockQuantity(dto.getProductCount());
            OrderingDetails od =
                    OrderingDetails.builder()
                            .product(productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티를 찾을 수 없습니다.")))
                            .quantity(dto.getProductCount())
                            .ordering(ordering).build();
            orderList.add(od);//cascade persist
        }
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
